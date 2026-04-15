package com.kevpierce.catholicfastingapp.notifications

import com.kevpierce.catholicfasting.core.model.AppDeepLinks

internal object ReminderNotificationRoutes {
    const val KIND_REQUIRED_DAY = "required_day"
    const val KIND_SUPPORT = "support"
    const val KIND_MORNING = "morning"
    const val KIND_EVENING = "evening"

    fun deepLinkForKind(kind: String): String =
        when (kind) {
            KIND_REQUIRED_DAY -> AppDeepLinks.CALENDAR
            else -> AppDeepLinks.TODAY
        }

    fun opensCalendar(deepLink: String): Boolean = deepLink == AppDeepLinks.CALENDAR
}
