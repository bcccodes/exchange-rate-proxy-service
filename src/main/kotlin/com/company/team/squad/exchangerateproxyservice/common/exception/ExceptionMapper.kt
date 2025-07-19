package com.company.team.squad.exchangerateproxyservice.common.exception

import com.company.team.squad.exchangerateproxyservice.common.Constants.CONTEXT_CORRELATION
import com.company.team.squad.exchangerateproxyservice.common.Constants.Response.RESPONSE_ERROR
import com.company.team.squad.exchangerateproxyservice.common.Response
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import java.util.concurrent.TimeoutException

@Component
class ExceptionMapper {
    fun toResponse(exception: Throwable, request: ServerRequest): Response<Unit> {
        val correlationId = request.attribute(CONTEXT_CORRELATION).orElse("default correlation") as String
        return when (exception) {
            is NoContentException -> toNoContentErrorResponse(correlationId)
            is TimeoutException, is ServiceTimeoutException -> toTimeOutErrorResponse(correlationId)
            is CallNotPermittedException -> toCircuitBreakerErrorResponse(correlationId)
            is RequestNotPermitted -> toRateLimitErrorResponse(correlationId)
            else -> toDefaultErrorResponse(correlationId)
        }
    }

    private fun toNoContentErrorResponse(correlationId: String) =
        toErrorResponse(ErrorMessage.NO_CONTENT, correlationId)

    private fun toRateLimitErrorResponse(correlationId: String) =
        toErrorResponse(ErrorMessage.RATE_LIMIT_EXCEEDED, correlationId)

    private fun toTimeOutErrorResponse(correlationId: String) =
        toErrorResponse(ErrorMessage.TIMEOUT, correlationId)

    private fun toCircuitBreakerErrorResponse(correlationId: String) =
        toErrorResponse(ErrorMessage.CIRCUIT_BREAKER_OPEN, correlationId)

    private fun toDefaultErrorResponse(correlationId: String) =
        toErrorResponse(ErrorMessage.UNKNOWN_ERROR, correlationId)

    private fun toErrorResponse(errorMessage: ErrorMessage, correlationId: String) = Response<Unit>(
        status = RESPONSE_ERROR,
        error = Response.Error(
            status = errorMessage.httpStatus.value(),
            code = errorMessage.httpStatus.name,
            error = errorMessage.httpStatus.reasonPhrase,
            message = errorMessage.message,
            traceId = correlationId
        )
    )

}