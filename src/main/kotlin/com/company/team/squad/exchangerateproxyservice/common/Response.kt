package com.company.team.squad.exchangerateproxyservice.common

import com.company.team.squad.exchangerateproxyservice.common.Constants.Response.RESPONSE_OK
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.json

@JsonInclude(NON_NULL)
data class Response<T>(
    val status: String? = null,
    val data: T? = null,
    val error: Error? = null,
) {
    @JsonInclude(NON_NULL)
    data class Error(
        val status: Int? = null,
        val code: String? = null,
        val error: String? = null,
        val message: Any? = null,
        val traceId: String? = null,
    )
}

suspend fun <T> response(data: T? = null) =
    ServerResponse.ok().json().bodyValueAndAwait(Response(RESPONSE_OK, data))