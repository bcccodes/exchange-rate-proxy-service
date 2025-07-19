package com.company.team.squad.exchangerateproxyservice.common

object Constants {

    const val EMPTY_STRING = ""
    const val UNKNOWN_VALUE = "N/A"
    const val HEADER_CORRELATION = "x-correlation-id"
    const val HEADER_API_KEY = "x-api-key"
    const val CONTEXT_CORRELATION = "correlationId"
    const val ERROR_RESPONSE_KEY = "error_response"

    object Response {
        const val RESPONSE_OK = "success"
        const val RESPONSE_ERROR = "error"
    }

    object Features {
        const val CURRENCY = "currency"
        const val CONVERSION = "conversion"
        const val HISTORY = "history"
    }
}