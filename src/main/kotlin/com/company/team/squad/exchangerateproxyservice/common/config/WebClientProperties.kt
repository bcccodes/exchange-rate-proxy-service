package com.company.team.squad.exchangerateproxyservice.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "settings.web-client")
data class WebClientProperties(
    val timeOut: Duration,
    val exchangeRate: ExchangeRate,
) {
    data class ExchangeRate(
        val baseUrl: String,
        val xApiKey: String,
        val currencyApi: CurrencyApi,
        val conversionApi: ConversionApi,
        val historyApi: HistoryApi,
    )

    data class CurrencyApi(
        val url: String,
    )

    data class ConversionApi(
        val url: String,
    )

    data class HistoryApi(
        val url: String,
    )
}