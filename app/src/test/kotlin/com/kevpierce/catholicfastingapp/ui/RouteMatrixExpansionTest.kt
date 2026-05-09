package com.kevpierce.catholicfastingapp.ui

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import org.junit.Test

class RouteMatrixExpansionTest {
    @Test
    fun everyPublicDeepLinkResolvesToAConcreteDestination() {
        publicDeepLinks().forEach { deepLink ->
            assertThat(AppRouteResolver.resolve(deepLink).topLevelDestination).isNotNull()
        }
    }

    @Test
    fun todayDeepLinkResolvesToTodayTab() {
        assertThat(AppRouteResolver.resolve(AppDeepLinks.TODAY).topLevelDestination).isEqualTo(TopLevelDestination.TODAY)
    }

    @Test
    fun calendarFridayNoteDeepLinkStaysOnFastingDaysTab() {
        assertThat(AppRouteResolver.resolve(AppDeepLinks.CALENDAR_FRIDAY_NOTE).topLevelDestination)
            .isEqualTo(TopLevelDestination.FASTING_DAYS)
    }

    @Test
    fun trackerDeepLinkResolvesToTrackFastTab() {
        assertThat(AppRouteResolver.resolve(AppDeepLinks.TRACKER).topLevelDestination)
            .isEqualTo(TopLevelDestination.TRACK_FAST)
    }

    @Test
    fun premiumDeepLinkResolvesToMorePremiumSection() {
        assertThat(AppRouteResolver.resolve(AppDeepLinks.MORE_PREMIUM).moreSection)
            .isEqualTo(MoreSection.SUPPORT_PREMIUM)
    }

    @Test
    fun setupDeepLinkResolvesToMoreSetupSection() {
        assertThat(AppRouteResolver.resolve(AppDeepLinks.MORE_SETUP).moreSection)
            .isEqualTo(MoreSection.SETUP_REMINDERS)
    }

    @Test
    fun historyDeepLinkResolvesToMoreHistorySection() {
        assertThat(AppRouteResolver.resolve(AppDeepLinks.MORE_HISTORY).moreSection)
            .isEqualTo(MoreSection.HISTORY_OF_FASTING)
    }

    @Test
    fun privacyDeepLinkResolvesToMorePrivacySection() {
        assertThat(AppRouteResolver.resolve(AppDeepLinks.MORE_PRIVACY).moreSection)
            .isEqualTo(MoreSection.PRIVACY_DATA)
    }

    @Test
    fun trailingSlashDoesNotChangeResolution() {
        assertThat(AppRouteResolver.resolve("${AppDeepLinks.MORE_SETUP}/").moreSection)
            .isEqualTo(MoreSection.SETUP_REMINDERS)
    }

    @Test
    fun duplicateSlashesDoNotChangeResolution() {
        assertThat(AppRouteResolver.resolve("catholicfasting://open//more//privacy").moreSection)
            .isEqualTo(MoreSection.PRIVACY_DATA)
    }

    @Test
    fun unknownNestedMoreRouteFallsBackToPremiumSection() {
        val destination = AppRouteResolver.resolve("catholicfasting://open/more/unknown/path")

        assertThat(destination.topLevelDestination).isEqualTo(TopLevelDestination.MORE)
        assertThat(destination.moreSection).isEqualTo(MoreSection.SUPPORT_PREMIUM)
    }

    @Test
    fun unrelatedSchemeWithKnownPathStillResolvesByPath() {
        assertThat(AppRouteResolver.resolve("https://example.test/more/history").moreSection)
            .isEqualTo(MoreSection.HISTORY_OF_FASTING)
    }

    @Test
    fun blankDeepLinkDefaultsToToday() {
        assertThat(AppRouteResolver.resolve("   ").topLevelDestination).isEqualTo(TopLevelDestination.TODAY)
    }

    @Test
    fun publicDeepLinkConstantsUseExpectedSchemeAndHost() {
        publicDeepLinks().forEach { deepLink ->
            assertThat(deepLink).startsWith("${AppDeepLinks.SCHEME}://${AppDeepLinks.HOST}/")
        }
    }

    private fun publicDeepLinks(): List<String> =
        listOf(
            AppDeepLinks.TODAY,
            AppDeepLinks.CALENDAR,
            AppDeepLinks.TRACKER,
            AppDeepLinks.MORE_PREMIUM,
            AppDeepLinks.MORE_SETUP,
            AppDeepLinks.MORE_HISTORY,
            AppDeepLinks.MORE_PRIVACY,
            AppDeepLinks.CALENDAR_FRIDAY_NOTE,
        )
}
