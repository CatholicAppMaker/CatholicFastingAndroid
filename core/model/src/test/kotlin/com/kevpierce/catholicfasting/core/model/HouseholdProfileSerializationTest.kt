package com.kevpierce.catholicfasting.core.model

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class HouseholdProfileSerializationTest {
    @Test
    fun decodesLegacyBirthYearOnlyPayload() {
        val json =
            """
            [
              {
                "id": "legacy-profile",
                "name": "Legacy",
                "birthYear": 1985,
                "medicalDispensation": true
              }
            ]
            """.trimIndent()

        val decoded = Json.decodeFromString<List<HouseholdProfile>>(json)
        val profile = decoded.first()

        assertThat(profile.isAge14OrOlderForAbstinence).isTrue()
        assertThat(profile.isAge18OrOlderForFasting).isTrue()
        assertThat(profile.medicalDispensation).isTrue()
    }
}
