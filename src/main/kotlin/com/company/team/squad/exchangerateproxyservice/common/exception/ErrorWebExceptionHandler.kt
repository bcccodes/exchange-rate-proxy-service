package com.company.team.squad.exchangerateproxyservice.common.exception

import com.company.team.squad.exchangerateproxyservice.common.Constants.ERROR_RESPONSE_KEY
import com.company.team.squad.exchangerateproxyservice.common.Response
import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

@Configuration
class ErrorWebExceptionHandler(
    errorAttributes: ErrorAttributes,
    webProperties: WebProperties,
    applicationContext: ApplicationContext,
    serverCodecConfigurer: ServerCodecConfigurer
) : DefaultErrorWebExceptionHandler(
    errorAttributes,
    webProperties.resources,
    ErrorProperties(),
    applicationContext
) {

    init {
        super.setMessageWriters(serverCodecConfigurer.writers)
        super.setMessageReaders(serverCodecConfigurer.readers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse>? =
        RouterFunctions.route(RequestPredicates.all()) {
            renderErrorResponse(it)
        }


    override fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> =
        getErrorResponse(request).let {
            ServerResponse.status(it.status!!)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(it)
        }

    override fun logError(request: ServerRequest, response: ServerResponse, throwable: Throwable) = Unit

    private fun getErrorResponse(request: ServerRequest): Response.Error {

        val errorAttributes = getErrorAttributes(request, ErrorAttributeOptions.defaults())
        val response = errorAttributes[ERROR_RESPONSE_KEY] as? Response<Unit>
        return response?.error ?: Response.Error()
    }


}