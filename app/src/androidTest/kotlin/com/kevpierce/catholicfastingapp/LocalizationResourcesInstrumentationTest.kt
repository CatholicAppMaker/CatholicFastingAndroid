package com.kevpierce.catholicfastingapp

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.kevpierce.catholicfasting.feature.calendar.R as CalendarR
import com.kevpierce.catholicfasting.feature.guidance.R as GuidanceR
import com.kevpierce.catholicfasting.feature.premium.R as PremiumR
import com.kevpierce.catholicfasting.feature.settings.R as SettingsR
import com.kevpierce.catholicfasting.feature.today.R as TodayR
import com.kevpierce.catholicfasting.feature.tracker.R as TrackerR

@RunWith(AndroidJUnit4::class)
class LocalizationResourcesInstrumentationTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun onboardingSpanishResourcesResolveCoreCopy() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, R.string.onboarding_title)
        assertLocalizedString(spanish, R.string.onboarding_notice_title)
        assertLocalizedString(spanish, R.string.onboarding_intention_title)
        assertLocalizedString(spanish, R.string.label_reminder_guided_summary)
    }

    @Test
    fun onboardingFrenchCanadianResourcesResolveCoreCopy() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, R.string.onboarding_title)
        assertLocalizedString(french, R.string.onboarding_notice_title)
        assertLocalizedString(french, R.string.onboarding_intention_title)
        assertLocalizedString(french, R.string.label_region_canada)
    }

    @Test
    fun premiumSpanishResourcesResolvePlanCopy() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, PremiumR.string.premium_title)
        assertLocalizedString(spanish, PremiumR.string.premium_subscriptions_title)
        assertLocalizedString(spanish, PremiumR.string.premium_reflection_journal_title)
    }

    @Test
    fun premiumFrenchCanadianResourcesResolvePlanCopy() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, PremiumR.string.premium_title)
        assertLocalizedString(french, PremiumR.string.premium_subscriptions_title)
        assertLocalizedString(french, PremiumR.string.premium_reflection_journal_title)
    }

    @Test
    fun trackerSpanishResourcesResolveCoreControls() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, TrackerR.string.tracker_title)
        assertLocalizedString(spanish, TrackerR.string.tracker_start_fast)
        assertLocalizedString(spanish, TrackerR.string.tracker_custom_schedules)
    }

    @Test
    fun trackerFrenchCanadianResourcesResolveCoreControls() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, TrackerR.string.tracker_title)
        assertLocalizedString(french, TrackerR.string.tracker_start_fast)
        assertLocalizedString(french, TrackerR.string.tracker_custom_schedules)
    }

    @Test
    fun guidanceSpanishResourcesResolveCoreSections() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, GuidanceR.string.guidance_title)
        assertLocalizedString(spanish, GuidanceR.string.guidance_food_title)
        assertLocalizedString(spanish, GuidanceR.string.guidance_rule_audit_title)
    }

    @Test
    fun guidanceFrenchCanadianResourcesResolveCoreSections() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, GuidanceR.string.guidance_title)
        assertLocalizedString(french, GuidanceR.string.guidance_food_title)
        assertLocalizedString(french, GuidanceR.string.guidance_rule_audit_title)
    }

    @Test
    fun calendarSpanishResourcesResolveCoreSections() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, CalendarR.string.calendar_title)
        assertLocalizedString(spanish, CalendarR.string.calendar_search_label)
        assertLocalizedString(spanish, CalendarR.string.calendar_progress_overview)
    }

    @Test
    fun calendarFrenchCanadianResourcesResolveCoreSections() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, CalendarR.string.calendar_title)
        assertLocalizedString(french, CalendarR.string.calendar_search_label)
        assertLocalizedString(french, CalendarR.string.calendar_progress_overview)
    }

    @Test
    fun settingsSpanishResourcesResolveCoreSections() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, SettingsR.string.settings_more_title)
        assertLocalizedString(spanish, SettingsR.string.settings_region)
        assertLocalizedString(spanish, SettingsR.string.settings_birth_year)
    }

    @Test
    fun settingsFrenchCanadianResourcesResolveCoreSections() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, SettingsR.string.settings_more_title)
        assertLocalizedString(french, SettingsR.string.settings_region)
        assertLocalizedString(french, SettingsR.string.settings_birth_year)
    }

    @Test
    fun todaySpanishResourcesResolveCoreSections() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, TodayR.string.today_title)
        assertLocalizedString(spanish, TodayR.string.today_year_plan_title)
        assertLocalizedString(spanish, TodayR.string.today_important_notice_title)
    }

    @Test
    fun todayFrenchCanadianResourcesResolveCoreSections() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, TodayR.string.today_title)
        assertLocalizedString(french, TodayR.string.today_year_plan_title)
        assertLocalizedString(french, TodayR.string.today_important_notice_title)
    }

    private fun localizedContext(languageTags: String): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocales(LocaleList.forLanguageTags(languageTags))
        return context.createConfigurationContext(configuration)
    }

    private fun assertLocalizedString(
        localizedContext: Context,
        resId: Int,
    ) {
        val value = localizedContext.getString(resId)
        assertThat(value.trim()).isNotEmpty()
    }
}
