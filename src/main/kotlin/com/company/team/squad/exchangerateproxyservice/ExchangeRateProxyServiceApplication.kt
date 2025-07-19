package com.company.team.squad.exchangerateproxyservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ExchangeRateProxyServiceApplication

fun main(args: Array<String>) {
    runApplication<ExchangeRateProxyServiceApplication>(*args)
}
