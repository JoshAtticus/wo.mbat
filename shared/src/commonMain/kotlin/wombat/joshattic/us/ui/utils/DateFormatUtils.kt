@file:OptIn(kotlin.time.ExperimentalTime::class)

package wombat.joshattic.us.ui.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val MONTHS_SHORT = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

fun formatMonthYear(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val month = MONTHS_SHORT[(dt.monthNumber - 1).coerceIn(0, 11)]
    return "$month ${dt.year}"
}

fun formatDayMonthYear(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val month = MONTHS_SHORT[(dt.monthNumber - 1).coerceIn(0, 11)]
    return "${dt.dayOfMonth} $month ${dt.year}"
}

fun formatShortNumericDate(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val dd = dt.dayOfMonth.toString().padStart(2, '0')
    val mm = dt.monthNumber.toString().padStart(2, '0')
    return "$dd/$mm/${dt.year}"
}
