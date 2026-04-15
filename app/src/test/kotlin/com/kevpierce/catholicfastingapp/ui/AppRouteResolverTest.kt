package com.kevpierce.catholicfastingapp.ui

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import org.junit.Test

class AppRouteResolverTest {
    @Test
    fun resolveDefaultsToTodayForNullOrUnknownLinks() {
        assertThat(AppRouteResolver.resolve(null))
            .isEqualTo(
                AppLaunchDestination(
                    topLevelDestination = TopLevelDestination.TODAY,
                    moreSection = MoreSection.SUPPORT_PREMIUM,
                ),
            )

        assertThat(AppRouteResolver.resolve("catholicfasting://open/unknown"))
            .isEqualTo(
                AppLaunchDestination(
                    topLevelDestination = TopLevelDestination.TODAY,
                    moreSection = MoreSection.SUPPORT_PREMIUM,
                ),
            )
    }

    @Test
    fun resolveMapsCalendarAndTrackerRoutes() {
        assertThat(AppRouteResolver.resolve("catholicfasting://open/calendar").topLevelDestination)
            .isEqualTo(TopLevelDestination.FASTING_DAYS)
        assertThat(AppRouteResolver.resolve("catholicfasting://open/calendar/friday-note").topLevelDestination)
            .isEqualTo(TopLevelDestination.FASTING_DAYS)
        assertThat(AppRouteResolver.resolve("catholicfasting://open/tracker").topLevelDestination)
            .isEqualTo(TopLevelDestination.TRACK_FAST)
    }

    @Test
    fun resolveMapsMoreSectionsByPathSegment() {
        assertThat(AppRouteResolver.resolve("catholicfasting://open/more/setup").moreSection)
            .isEqualTo(MoreSection.SETUP_REMINDERS)
        assertThat(AppRouteResolver.resolve("catholicfasting://open/more/profile").moreSection)
            .isEqualTo(MoreSection.PROFILE_NORMS)
        assertThat(AppRouteResolver.resolve("catholicfasting://open/more/guidance").moreSection)
            .isEqualTo(MoreSection.GUIDANCE_RULES)
        assertThat(AppRouteResolver.resolve("catholicfasting://open/more/privacy").moreSection)
            .isEqualTo(MoreSection.PRIVACY_DATA)
    }

    @Test
    fun resolveFallsBackToSupportPremiumForGenericMoreRoute() {
        assertThat(AppRouteResolver.resolve(AppDeepLinks.MORE_PREMIUM))
            .isEqualTo(
                AppLaunchDestination(
                    topLevelDestination = TopLevelDestination.MORE,
                    moreSection = MoreSection.SUPPORT_PREMIUM,
                ),
            )
    }

    @Test
    fun resolveSupportsPublicMoreDeepLinksAndQueryParameters() {
        assertThat(AppRouteResolver.resolve(AppDeepLinks.MORE_SETUP).moreSection)
            .isEqualTo(MoreSection.SETUP_REMINDERS)
        assertThat(AppRouteResolver.resolve(AppDeepLinks.MORE_PRIVACY).moreSection)
            .isEqualTo(MoreSection.PRIVACY_DATA)
        assertThat(AppRouteResolver.resolve("${AppDeepLinks.MORE_SETUP}?source=shortcut"))
            .isEqualTo(
                AppLaunchDestination(
                    topLevelDestination = TopLevelDestination.MORE,
                    moreSection = MoreSection.SETUP_REMINDERS,
                ),
            )
    }
}
