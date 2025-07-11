package kr.b1ink.data.util

import java.util.Date
import kotlin.math.roundToInt

fun Date.toTimeAgo(): String {
    if (time < 1000000000000L) {
        time *= 1000
    }
    val now = System.currentTimeMillis()

    val diff = now - time
    val seconds = diff / 1000
    val minutes = seconds / 60.0
    val hours = minutes / 60.0
    val days = hours / 24.0
    val months = days / 30.0
    val years = days / 365.0

    return when {
        seconds < 45 -> "방금 전"
        seconds < 90 -> "약 1분 전"
        minutes < 45 -> "${minutes.roundToInt()}분 전"
        minutes < 90 -> "약 1시간 전"
        hours < 24 -> "${hours.roundToInt()}시간 전"
//        hours < 48 -> "어제"
        days < 30 -> "${days.roundToInt()}일 전"
        days < 60 -> "약 1달 전"
        days < 365 -> "${months.roundToInt()}달 전"
        else -> "${years.roundToInt()}년 전"
    }
}
