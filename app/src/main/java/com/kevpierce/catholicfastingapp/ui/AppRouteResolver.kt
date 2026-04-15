package com.kevpierce.catholicfastingapp.ui

import java.net.URI

internal enum class TopLevelDestination {
    TODAY,
    FASTING_DAYS,
    TRACK_FAST,
    MORE,
}

internal enum class MoreSection {
    SUPPORT_PREMIUM,
    SETUP_REMINDERS,
    PROFILE_NORMS,
    GUIDANCE_RULES,
    PRIVACY_DATA,
}

internal data class AppLaunchDestination(
    val topLevelDestination: TopLevelDestination,
    val moreSection: MoreSection = MoreSection.SUPPORT_PREMIUM,
)

internal object AppRouteResolver {
    fun resolve(deepLink: String?): AppLaunchDestination {
        val pathSegments = parsePathSegments(deepLink)

        if (pathSegments.isEmpty()) {
            return AppLaunchDestination(topLevelDestination = TopLevelDestination.TODAY)
        }

        return when (pathSegments.first()) {
            "calendar" ->
                AppLaunchDestination(
                    topLevelDestination = TopLevelDestination.FASTING_DAYS,
                )

            "tracker" ->
                AppLaunchDestination(
                    topLevelDestination = TopLevelDestination.TRACK_FAST,
                )

            "more" ->
                AppLaunchDestination(
                    topLevelDestination = TopLevelDestination.MORE,
                    moreSection =
                        when (pathSegments.getOrNull(1)) {
                            "setup" -> MoreSection.SETUP_REMINDERS
                            "profile" -> MoreSection.PROFILE_NORMS
                            "guidance" -> MoreSection.GUIDANCE_RULES
                            "privacy" -> MoreSection.PRIVACY_DATA
                            else -> MoreSection.SUPPORT_PREMIUM
                        },
                )

            else -> AppLaunchDestination(topLevelDestination = TopLevelDestination.TODAY)
        }
    }

    private fun parsePathSegments(deepLink: String?): List<String> {
        if (deepLink.isNullOrBlank()) {
            return emptyList()
        }

        val path =
            runCatching { URI(deepLink).path }
                .getOrDefault("")

        return path
            .split('/')
            .map(String::trim)
            .filter(String::isNotBlank)
            .map(String::lowercase)
    }
}
