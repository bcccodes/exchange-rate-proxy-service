package com.company.team.squad.exchangerateproxyservice.currency.usecase

import com.company.team.squad.exchangerateproxyservice.common.config.WebClientProperties
import com.company.team.squad.exchangerateproxyservice.currency.client.CurrencyClient
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.timelimiter.TimeLimiter
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeoutException

class GetCurrenciesUsecaseTest {

    private val client: CurrencyClient = mockk<CurrencyClient>()
    private val webClientProperties = mockk<WebClientProperties> {
        every { exchangeRate.baseUrl } returns "https://currencylayermock.com"
        every { exchangeRate.currencyApi.url } returns "/currency"
    }

    private val defaultCircuitBreaker: CircuitBreaker = CircuitBreaker.ofDefaults("defaultCircuitBreaker")
    private val defaultRetry: Retry = Retry.ofDefaults("defaultRetry")
    private val defaultRateLimiter: RateLimiter = RateLimiter.ofDefaults("defaultRateLimiter")
    private val defaultTimeLimiter: TimeLimiter = TimeLimiter.ofDefaults("defaultTimeLimiter")
    private val circuitBreaker: CircuitBreaker = CircuitBreaker.of(
        "currencyCircuitBreaker",
        CircuitBreakerConfig.custom()
            .slidingWindowSize(5)
            .minimumNumberOfCalls(3)
            .permittedNumberOfCallsInHalfOpenState(1)
            .waitDurationInOpenState(Duration.ofMillis(100))
            .failureRateThreshold(50f)
            .build()
    )

    private val response = run {
        val jsonResponse =
            """
            {
                "status": "success",
                "data": {
                    "success": true,
                    "terms": "https://currencylayermock.com/terms",
                    "privacy": "https://currencylayermock.com/privacy",
                    "currencies": {
                        "AED": "United Arab Emirates Dirham",
                        "AFN": "Afghan Afghani",
                        "ALL": "Albanian Lek",
                        "AMD": "Armenian Dram",
                        "ANG": "Netherlands Antillean Guilder"
                    }
                }
            }
            """.trimIndent()
        jacksonObjectMapper().readTree(jsonResponse)
    }

    @Test
    fun `execute should return currencies when currencies exist`() = runTest {

        val getCurrenciesUsecase = GetCurrenciesUsecase(
            currencyClient = client,
            webClientProperties = webClientProperties,
            exchangeRateCircuitBreaker = defaultCircuitBreaker,
            exchangeRateRetry = defaultRetry,
            exchangeRateRateLimiter = defaultRateLimiter,
            exchangeRateTimeLimiter = defaultTimeLimiter,
        )
        coEvery { client.get(any()) } returns response

        val actual = getCurrenciesUsecase.execute()
        assertEquals(response, actual)
        coVerify(exactly = 1) { client.get("/currency") }
    }

    @Test
    fun `execute should throw CallNotPermittedException when circuit breaker is open`() = runTest {

        val getCurrenciesUsecase = GetCurrenciesUsecase(
            currencyClient = client,
            webClientProperties = webClientProperties,
            exchangeRateCircuitBreaker = circuitBreaker,
            exchangeRateRetry = defaultRetry,
            exchangeRateRateLimiter = defaultRateLimiter,
            exchangeRateTimeLimiter = defaultTimeLimiter,
        )
        coEvery { client.get(any()) } throws IOException("mock error message")
        repeat(3) {
            try {
                getCurrenciesUsecase.execute()
            } catch (ex: Exception) {

            }
        }

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.state)
        val actual = assertThrows<CallNotPermittedException> {
            getCurrenciesUsecase.execute()
        }
        assertEquals(
            "CircuitBreaker '${circuitBreaker.name}' is OPEN and does not permit further calls",
            actual.message
        )
        coVerify(exactly = 3) { client.get("/currency") }
    }

    @Test
    fun `execute should return currencies when circuit breaker is half-open and client call succeeds`() = runTest {

        val getCurrenciesUsecase = GetCurrenciesUsecase(
            currencyClient = client,
            webClientProperties = webClientProperties,
            exchangeRateCircuitBreaker = circuitBreaker,
            exchangeRateRetry = defaultRetry,
            exchangeRateRateLimiter = defaultRateLimiter,
            exchangeRateTimeLimiter = defaultTimeLimiter,
        )
        coEvery { client.get(any()) } returns response
        circuitBreaker.transitionToOpenState()
        circuitBreaker.transitionToHalfOpenState()

        val actual = getCurrenciesUsecase.execute()
        assertEquals(response, actual)
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.state)
        coVerify(exactly = 1) { client.get("/currency") }
    }

    @Test
    fun `execute should throw Exception when max retry attempts are reached`() = runTest {

        val retry: Retry = Retry.of(
            "currencyRetry",
            RetryConfig.custom<Any>()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(10))
                .retryExceptions(IOException::class.java)
                .build()
        )
        val getCurrenciesUsecase = GetCurrenciesUsecase(
            currencyClient = client,
            webClientProperties = webClientProperties,
            exchangeRateCircuitBreaker = defaultCircuitBreaker,
            exchangeRateRetry = retry,
            exchangeRateRateLimiter = defaultRateLimiter,
            exchangeRateTimeLimiter = defaultTimeLimiter,
        )
        val errorMessage = "mock error message"
        coEvery { client.get(any()) } throws IOException(errorMessage)

        val actual = assertThrows<IOException> {
            getCurrenciesUsecase.execute()
        }
        assertEquals(errorMessage, actual.message)
        coVerify(exactly = 3) { client.get("/currency") }
    }

    @Test
    fun `execute should throw RequestNotPermitted when rate limit is exceeded`() = runTest {

        val rateLimiter: RateLimiter = RateLimiter.of(
            "currencyRateLimiter",
            RateLimiterConfig.custom()
                .limitForPeriod(1)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofMillis(0))
                .build()
        )
        val getCurrenciesUsecase = GetCurrenciesUsecase(
            currencyClient = client,
            webClientProperties = webClientProperties,
            exchangeRateCircuitBreaker = defaultCircuitBreaker,
            exchangeRateRetry = defaultRetry,
            exchangeRateRateLimiter = rateLimiter,
            exchangeRateTimeLimiter = defaultTimeLimiter,
        )
        coEvery { client.get(any()) } returns response

        getCurrenciesUsecase.execute()
        val actual = assertThrows<RequestNotPermitted> {
            getCurrenciesUsecase.execute()
        }
        assertEquals(0, rateLimiter.metrics.availablePermissions)
        assertEquals("RateLimiter '${rateLimiter.name}' does not permit further calls", actual.message)
        coVerify(exactly = 1) { client.get("/currency") }
    }

    @Test
    fun `execute should throw TimeoutException when time out`() = runTest {

        val currencyRetry: Retry = Retry.of(
            "currencyRetry",
            RetryConfig.custom<Any>()
                .retryExceptions(IOException::class.java)
                .build()
        )
        val timeLimiter: TimeLimiter = TimeLimiter.of(
            "currencyTimeLimiter",
            TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(500))
                .cancelRunningFuture(true)
                .build()
        )
        val getCurrenciesUsecase = GetCurrenciesUsecase(
            currencyClient = client,
            webClientProperties = webClientProperties,
            exchangeRateCircuitBreaker = defaultCircuitBreaker,
            exchangeRateRetry = currencyRetry,
            exchangeRateRateLimiter = defaultRateLimiter,
            exchangeRateTimeLimiter = timeLimiter,
        )
        coEvery { client.get(any()) } coAnswers {
            delay(800)
            response
        }

        val actual = assertThrows<TimeoutException> {
            getCurrenciesUsecase.execute()
        }
        assertEquals("TimeLimiter '${timeLimiter.name}' recorded a timeout exception.", actual.message)
        coVerify(exactly = 1) { client.get("/currency") }
    }
}