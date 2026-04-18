package com.kevpierce.catholicfastingapp

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.data.AppContainer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.kevpierce.catholicfasting.feature.guidance.R as GuidanceR
import com.kevpierce.catholicfasting.feature.premium.R as PremiumR

@RunWith(AndroidJUnit4::class)
class PrivacyLocalizationInstrumentationTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppContainer.initialize(context)
    }

    @Test
    fun privacyStringsResolveInDefaultResources() {
        assertThat(context.getString(R.string.more_privacy_title)).isEqualTo("Privacy & data")
        assertThat(context.getString(R.string.more_privacy_local_first)).contains("stay on-device")
        assertThat(context.getString(R.string.more_privacy_backup_tools))
            .contains("Android system backup is disabled")
        assertThat(context.getString(R.string.more_privacy_last_sync, "now"))
            .startsWith("Last local sync-style write:")
    }

    @Test
    fun privacyStringsResolveInSpanishResources() {
        val spanishContext = localizedContext("es-US")

        assertThat(spanishContext.getString(R.string.more_privacy_title)).isEqualTo("Privacidad y datos")
        assertThat(spanishContext.getString(R.string.more_privacy_local_first))
            .contains("permanece en el dispositivo")
        assertThat(spanishContext.getString(R.string.more_privacy_backup_tools))
            .contains("copia de seguridad del sistema Android esta desactivada")
        assertThat(spanishContext.getString(R.string.more_privacy_last_sync, "ahora"))
            .startsWith("Ultima escritura local tipo sincronizacion:")
    }

    @Test
    fun releaseLabelStringsResolveAcrossModulesInDefaultResources() {
        assertThat(context.getString(R.string.label_region_us)).isEqualTo("United States")
        assertThat(context.getString(R.string.label_reminder_guided_summary))
            .contains("morning and evening support")
        assertThat(context.getString(PremiumR.string.premium_catalog_subtitle))
            .contains("plan ahead")
        assertThat(context.getString(PremiumR.string.premium_billing_subscription_active))
            .isEqualTo("Premium subscription is active.")
        assertThat(context.getString(GuidanceR.string.guidance_region_other))
            .contains("local episcopal law")
    }

    @Test
    fun releaseLabelStringsResolveAcrossModulesInSpanishResources() {
        val spanishContext = localizedContext("es-US")

        assertThat(spanishContext.getString(R.string.label_region_us)).isEqualTo("Estados Unidos")
        assertThat(spanishContext.getString(R.string.label_reminder_guided_summary))
            .contains("matutino y vespertino")
        assertThat(spanishContext.getString(PremiumR.string.premium_catalog_subtitle))
            .contains("planificar con anticipacion")
        assertThat(spanishContext.getString(PremiumR.string.premium_billing_subscription_active))
            .isEqualTo("La suscripcion premium esta activa.")
        assertThat(spanishContext.getString(GuidanceR.string.guidance_region_other))
            .contains("guia pastoral")
    }

    private fun localizedContext(languageTags: String): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocales(
            LocaleList.forLanguageTags(languageTags),
        )
        return context.createConfigurationContext(configuration)
    }
}
