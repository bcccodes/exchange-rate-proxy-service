package com.company.team.squad.exchangerateproxyservice.common.exception

import com.company.team.squad.exchangerateproxyservice.common.Constants.ERROR_RESPONSE_KEY
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class ErrorAttributes(
    private val exceptionMapper: ExceptionMapper
) : DefaultErrorAttributes() {
    override fun getErrorAttributes(
        request: ServerRequest,
        options: ErrorAttributeOptions?
    ): MutableMap<String, Any>? {
        return mutableMapOf(ERROR_RESPONSE_KEY to exceptionMapper.toResponse(getError(request), request))
    }
}