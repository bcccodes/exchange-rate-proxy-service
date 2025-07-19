package com.company.team.squad.exchangerateproxyservice.common

import com.company.team.squad.exchangerateproxyservice.common.Constants.CONTEXT_CORRELATION
import com.company.team.squad.exchangerateproxyservice.common.Constants.UNKNOWN_VALUE
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.coroutines.reactor.ReactorContext
import kotlin.coroutines.coroutineContext

suspend fun getCorrelation(): String =
    coroutineContext[ReactorContext]?.context?.getOrDefault(CONTEXT_CORRELATION, UNKNOWN_VALUE) ?: UNKNOWN_VALUE

suspend inline fun KLogger.mdc(log: KLogger.() -> Unit) =
    withLoggingContext(CONTEXT_CORRELATION to getCorrelation()) { this.log() }

inline fun KLogger.mdc(correlation: String, log: KLogger.() -> Unit) =
    withLoggingContext(CONTEXT_CORRELATION to correlation) { this.log() }