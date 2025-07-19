package com.company.team.squad.exchangerateproxyservice.currency.client

import com.company.team.squad.exchangerateproxyservice.common.Constants.HEADER_CORRELATION
import com.company.team.squad.exchangerateproxyservice.common.exception.NoContentException
import com.company.team.squad.exchangerateproxyservice.common.exception.ServiceBadGatewayException
import com.company.team.squad.exchangerateproxyservice.common.exception.ServiceTimeoutException
import com.company.team.squad.exchangerateproxyservice.common.exception.UnhandledException
import com.company.team.squad.exchangerateproxyservice.common.getCorrelation
import com.fasterxml.jackson.databind.JsonNode
import io.netty.channel.ConnectTimeoutException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import org.springframework.web.reactive.function.client.awaitExchangeOrNull

@Service
class CurrencyClient(
    private val exchangeRateClient: WebClient,
) {
    suspend fun get(url: String) =
        try {
            exchangeRateClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .headers(buildHeader(getCorrelation()))
                .awaitExchangeOrNull {
                    it.awaitBodyOrNull<JsonNode>()?.let { body ->
                        when (it.statusCode()) {
                            HttpStatus.OK -> body
                            HttpStatus.BAD_GATEWAY -> throw ServiceBadGatewayException()
                            else -> throw UnhandledException(body.toString())
                        }
                    } ?: throw NoContentException("Response body is null")
                } ?: throw NoContentException("Exchange response is null")
        } catch (ex: WebClientRequestException) {
            when (ex.cause) {
                is ConnectTimeoutException -> throw ServiceTimeoutException()
                else -> throw UnhandledException()
            }
        }

    fun buildHeader(correlation: String): (HttpHeaders) -> Unit = {
        it[HEADER_CORRELATION] = correlation
        //it.setBasicAuth("user", "pass")
        //it.setBearerAuth("token")
    }
}