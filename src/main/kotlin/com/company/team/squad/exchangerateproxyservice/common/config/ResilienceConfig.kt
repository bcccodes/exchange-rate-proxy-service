package com.company.team.squad.exchangerateproxyservice.common.config

import com.company.team.squad.exchangerateproxyservice.common.Constants.Features.CURRENCY
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import io.github.resilience4j.retry.RetryRegistry
import io.github.resilience4j.timelimiter.TimeLimiterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ResilienceConfig {

    @Bean
    fun currencyCircuitBreaker(circuitBreakerRegistry: CircuitBreakerRegistry) =
        circuitBreakerRegistry.circuitBreaker(CURRENCY)

    @Bean
    fun currencyRateLimiter(rateLimiterRegistry: RateLimiterRegistry) =
        rateLimiterRegistry.rateLimiter(CURRENCY)

    @Bean
    fun currencyTimeLimiter(timeLimiterRegistry: TimeLimiterRegistry) =
        timeLimiterRegistry.timeLimiter(CURRENCY)

    @Bean
    fun currencyRetry(retryRegistry: RetryRegistry) =
        retryRegistry.retry(CURRENCY)
}