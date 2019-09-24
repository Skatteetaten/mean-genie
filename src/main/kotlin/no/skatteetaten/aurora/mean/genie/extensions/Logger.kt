package no.skatteetaten.aurora.mean.genie.extensions

import org.slf4j.Logger

fun Logger.errorStackTraceIfDebug(message: String, throwable: Throwable) =
    if (this.isDebugEnabled) {
        this.error(message, throwable)
    } else {
        this.error(message)
    }