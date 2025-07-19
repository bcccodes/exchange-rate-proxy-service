package com.company.team.squad.exchangerateproxyservice.common.exception


class NoContentException(
    message: String = ErrorMessage.NO_CONTENT.message
) : RuntimeException(message)

class ServiceBadGatewayException(
    message: String = ErrorMessage.BAD_GATEWAY.message
) : RuntimeException(message)

class ServiceTimeoutException(
    message: String = ErrorMessage.TIMEOUT.message
) : RuntimeException(message)

class UnhandledException(
    message: String = ErrorMessage.UNKNOWN_ERROR.message
) : RuntimeException(message)


