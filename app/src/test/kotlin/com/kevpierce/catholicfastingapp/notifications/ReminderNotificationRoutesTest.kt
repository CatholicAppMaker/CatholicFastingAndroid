package com.kevpierce.catholicfastingapp.notifications

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import org.junit.Test

class ReminderNotificationRoutesTest {
    @Test
    fun requiredDayReminderRoutesToCalendar() {
        assertThat(
            ReminderNotificationRoutes.deepLinkForKind(ReminderNotificationRoutes.KIND_REQUIRED_DAY),
        ).isEqualTo(AppDeepLinks.CALENDAR)
    }

    @Test
    fun supportAndCadenceRemindersRouteToToday() {
        assertThat(
            ReminderNotificationRoutes.deepLinkForKind(ReminderNotificationRoutes.KIND_SUPPORT),
        ).isEqualTo(AppDeepLinks.TODAY)
        assertThat(
            ReminderNotificationRoutes.deepLinkForKind(ReminderNotificationRoutes.KIND_MORNING),
        ).isEqualTo(AppDeepLinks.TODAY)
        assertThat(
            ReminderNotificationRoutes.deepLinkForKind(ReminderNotificationRoutes.KIND_EVENING),
        ).isEqualTo(AppDeepLinks.TODAY)
    }

    @Test
    fun calendarDetectionMatchesDeepLink() {
        assertThat(ReminderNotificationRoutes.opensCalendar(AppDeepLinks.CALENDAR)).isTrue()
        assertThat(ReminderNotificationRoutes.opensCalendar(AppDeepLinks.TODAY)).isFalse()
    }
}
