package com.company.team.squad.exchangerateproxyservice.currency

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class CurrencyRouter {

    @Bean
    fun currencyRoute(currencyHandler: CurrencyHandler) = coRouter {
        accept(MediaType.APPLICATION_JSON).nest {
            "/api/v1/currencies".nest {
                GET("", currencyHandler::getCurrencies)
            }
        }
    }
}