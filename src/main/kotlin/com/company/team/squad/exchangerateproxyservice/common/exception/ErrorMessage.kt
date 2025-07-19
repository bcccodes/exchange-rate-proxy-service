package com.company.team.squad.exchangerateproxyservice.common.exception

import org.springframework.http.HttpStatus

enum class ErrorMessage(
    val httpStatus: HttpStatus,
    val message: String
) {

    NO_CONTENT(
        HttpStatus.NO_CONTENT,
        "No content available"
    ),
    CIRCUIT_BREAKER_OPEN(
        HttpStatus.SERVICE_UNAVAILABLE,
        "The service is temporarily unavailable due to circuit breaker."
    ),
    BAD_GATEWAY(
        HttpStatus.BAD_GATEWAY,
        "A bad gateway error occurred."
    ),
    TIMEOUT(
        HttpStatus.GATEWAY_TIMEOUT,
        "The request to the service timed out."
    ),
    RATE_LIMIT_EXCEEDED(
        HttpStatus.TOO_MANY_REQUESTS,
        "Request rate exceeded the allowed threshold."
    ),
    UNKNOWN_ERROR(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred. Please try again later."
    );

    fun format(vararg args: Any?): String {
        return message.format(*args)
    }
}