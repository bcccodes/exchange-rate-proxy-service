package com.company.team.squad.exchangerateproxyservice.currency.usecase

import com.company.team.squad.exchangerateproxyservice.common.config.WebClientProperties
import com.company.team.squad.exchangerateproxyservice.common.mdc
import com.company.team.squad.exchangerateproxyservice.currency.client.CurrencyClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.kotlin.circuitbreaker.circuitBreaker
import io.github.resilience4j.kotlin.ratelimiter.rateLimiter
import io.github.resilience4j.kotlin.retry.retry
import io.github.resilience4j.kotlin.timelimiter.timeLimiter
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.timelimiter.TimeLimiter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class GetCurrenciesUsecase(
    private val currencyClient: CurrencyClient,
    private val webClientProperties: WebClientProperties,
    private val exchangeRateCircuitBreaker: CircuitBreaker,
    private val exchangeRateRetry: Retry,
    private val exchangeRateRateLimiter: RateLimiter,
    private val exchangeRateTimeLimiter: TimeLimiter,
) {
    suspend fun execute() = with(webClientProperties.exchangeRate.currencyApi) {
        val startTime = System.currentTimeMillis()
        flow {
            emit(currencyClient.get(url))
        }.onStart {
            logger.mdc { info { "Start calling API: $url" } }
        }.onCompletion { cause ->
            val duration = System.currentTimeMillis() - startTime
            if (cause == null) {
                logger.mdc { info { "Success calling API: $url (took ${duration}ms)" } }
            } else {
                logger.mdc { error { "Failed calling API: $url ${cause.message}" } }
            }
        }.circuitBreaker(exchangeRateCircuitBreaker)
            .rateLimiter(exchangeRateRateLimiter)
            .timeLimiter(exchangeRateTimeLimiter)
            .retry(exchangeRateRetry)
            .first()
    }
}