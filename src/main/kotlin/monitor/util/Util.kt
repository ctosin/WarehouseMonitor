package monitor.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

fun fail(message: String): Nothing {
    throw IllegalStateException(message)
}

fun Long.formatTimestamp(timestampFormat: DateTimeFormatter = ISO_LOCAL_DATE_TIME): String {
    return Instant
        .ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(timestampFormat)
}
