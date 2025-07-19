package com.company.team.squad.exchangerateproxyservice.common.filter

import com.company.team.squad.exchangerateproxyservice.common.Constants.CONTEXT_CORRELATION
import com.company.team.squad.exchangerateproxyservice.common.Constants.HEADER_CORRELATION
import com.company.team.squad.exchangerateproxyservice.common.mdc
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.*

private val logger = KotlinLogging.logger { }

@Component
class GlobalWebFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {

        val request = exchange.request
        val response = exchange.response
        val correlationId = request.headers[HEADER_CORRELATION]?.firstOrNull() ?: generateCorrelationId()

        //MDC.put(CONTEXT_CORRELATION, correlationId)
        exchange.attributes[CONTEXT_CORRELATION] = correlationId
        logRequest(request, correlationId)
        logResponse(request, response, correlationId)
        return chain.filter(exchange)
            .contextWrite { it.put(CONTEXT_CORRELATION, correlationId) }
    }

    private fun logRequest(request: ServerHttpRequest, correlationId: String) = with(request) {
        logger.mdc(correlationId) {
            info { "Request ${method.name()} ${path.value()}" }
        }
    }

    private fun logResponse(request: ServerHttpRequest, response: ServerHttpResponse, correlationId: String) {
        response.beforeCommit {
            val status = response.statusCode?.value() ?: 0
            logger.mdc(correlationId) {
                info { "Response ${request.method.name()} $status" }
            }
            Mono.empty()
        }
    }

    private fun generateCorrelationId(): String = UUID.randomUUID().toString()
}