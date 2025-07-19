package com.company.team.squad.exchangerateproxyservice.currency

import com.company.team.squad.exchangerateproxyservice.common.response
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest


private val mapper = jacksonMapperBuilder()
    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    .build()
private val logger = KotlinLogging.logger { }

@Component
class CurrencyHandler(
    private val currencyFacade: CurrencyFacade
) {
    suspend fun getCurrencies(serverRequest: ServerRequest) =
        response(currencyFacade.getCurrencies())

//    fun get(serverRequest: ServerRequest) =
//        serverRequest.awaitBodyOrNull<String>()?.let { jsonRequest ->
//            currencyFacade.getCurrencies(jsonRequest)
//            ServerResponse
//                .ok()
//                .json()
//                .bodyValueAndAwait(
//                    mapper.writeValueAsString("Test")
//                )
//            //mapper.readValue(jsonRequest, JsonNode::class.java)
//        } ?: let {
//            logger.error { "Request Body is empty." }
//            throw RuntimeException("")
//        }


    //serverRequest.pathVariables() //this["user_id"]
    //serverRequest.queryParams()
}