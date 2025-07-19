package com.company.team.squad.exchangerateproxyservice.common.config

import com.company.team.squad.exchangerateproxyservice.common.Constants.HEADER_API_KEY
import com.company.team.squad.exchangerateproxyservice.common.Constants.HEADER_CORRELATION
import com.company.team.squad.exchangerateproxyservice.common.Constants.UNKNOWN_VALUE
import com.company.team.squad.exchangerateproxyservice.common.mdc
import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelOption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

private val logger = KotlinLogging.logger { }

@Configuration
class WebClientConfig(
    private val webClientProperties: WebClientProperties,
) {

    @Bean
    fun exchangeRateClient(): WebClient = with(webClientProperties.exchangeRate) {
        WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeaders { it[HEADER_API_KEY] = xApiKey }
            .filter(webClientFilter())
            .clientConnector(ReactorClientHttpConnector(httpClient()))
            .build()
    }

    fun webClientFilter(): ExchangeFilterFunction =
        ExchangeFilterFunction.ofRequestProcessor { request ->
            val headers = request.headers()
            logger.mdc(getCorrelationId(headers)) {
                debug { "webClient >> Method: ${request.method()}, URL: ${request.url()}, headers: $headers" }
            }
            Mono.just(request)
        }

    fun getCorrelationId(headers: HttpHeaders) = headers.getFirst(HEADER_CORRELATION) ?: UNKNOWN_VALUE

    private fun httpClient() = with(webClientProperties.timeOut.toMillis()) {
        HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.toInt())
            .responseTimeout(Duration.ofSeconds(this))
    }

    private val connectionProvider = ConnectionProvider.builder("fixed-connection-provider")
        .maxConnections(100)
        .pendingAcquireTimeout(Duration.ofSeconds(30))
        .pendingAcquireMaxCount(200)
        .maxIdleTime(Duration.ofSeconds(30))
        .maxLifeTime(Duration.ofMinutes(1))
        .build()
}