package com.kevpierce.catholicfasting.core.data

import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.FastingPlanningData
import com.kevpierce.catholicfasting.core.model.HouseholdProfile
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.IntermittentSchedulePlan
import com.kevpierce.catholicfasting.core.model.LaunchFunnelSnapshot
import com.kevpierce.catholicfasting.core.model.PremiumChecklistItem
import com.kevpierce.catholicfasting.core.model.PremiumCompanionState
import com.kevpierce.catholicfasting.core.model.ReflectionJournalEntry
import com.kevpierce.catholicfasting.core.model.RuleSettings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.time.Instant
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Serializable
internal data class EncryptedBackupEnvelope(
    val schemaVersion: Int,
    val exportedAtIso: String,
    val saltBase64: String,
    val ivBase64: String,
    val cipherTextBase64: String,
)

@Serializable
internal data class ExportPayload(
    val schemaVersion: Int,
    val exportedAtIso: String,
    val settings: RuleSettings,
    val year: Int,
    val statusesById: Map<String, CompletionStatus>,
    val fridayNotesById: Map<String, String>,
    val planningData: FastingPlanningData,
    val schedules: List<IntermittentSchedulePlan>,
    val intermittentSessions: List<IntermittentFastSession>,
    val activeIntermittentFast: ActiveIntermittentFast? = null,
    val intermittentPresetHours: Int,
    val profiles: List<HouseholdProfile>,
    val reflections: List<ReflectionJournalEntry>,
    val checklist: List<PremiumChecklistItem>,
    val premiumCompanionState: PremiumCompanionState,
    val launchFunnelSnapshot: LaunchFunnelSnapshot,
)

@Serializable
internal data class HouseholdSharePacket(
    val generatedAtIso: String,
    val planningData: FastingPlanningData,
    val schedules: List<IntermittentSchedulePlan>,
    val checklist: List<PremiumChecklistItem>,
)

private const val EXPORT_SCHEMA_VERSION = 1
private const val PBKDF2_ITERATIONS = 120_000
private const val AES_KEY_LENGTH_BITS = 256
private const val GCM_TAG_LENGTH_BITS = 128
private const val SALT_LENGTH_BYTES = 16
private const val IV_LENGTH_BYTES = 12

private val exportJson =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

internal object ExportCodec {
    fun createEncryptedBackup(
        state: DashboardState,
        passphrase: String,
    ): String {
        val normalizedPassphrase = passphrase.trim()
        require(normalizedPassphrase.length >= 8) {
            "Use a backup passphrase with at least 8 characters."
        }

        val payload =
            ExportPayload(
                schemaVersion = EXPORT_SCHEMA_VERSION,
                exportedAtIso = Instant.now().toString(),
                settings = state.settings,
                year = state.year,
                statusesById = state.statusesById,
                fridayNotesById = state.fridayNotesById,
                planningData = state.planningData,
                schedules = state.schedules,
                intermittentSessions = state.intermittentSessions,
                activeIntermittentFast = state.activeIntermittentFast,
                intermittentPresetHours = state.intermittentPresetHours,
                profiles = state.profiles,
                reflections = state.reflections,
                checklist = state.checklist,
                premiumCompanionState = state.premiumCompanionState,
                launchFunnelSnapshot = state.launchFunnelSnapshot,
            )
        val salt = randomBytes(SALT_LENGTH_BYTES)
        val iv = randomBytes(IV_LENGTH_BYTES)
        val cipherText =
            encrypt(
                plainText = exportJson.encodeToString(payload).encodeToByteArray(),
                passphrase = normalizedPassphrase,
                salt = salt,
                iv = iv,
            )

        return exportJson.encodeToString(
            EncryptedBackupEnvelope(
                schemaVersion = EXPORT_SCHEMA_VERSION,
                exportedAtIso = payload.exportedAtIso,
                saltBase64 = salt.toBase64(),
                ivBase64 = iv.toBase64(),
                cipherTextBase64 = cipherText.toBase64(),
            ),
        )
    }

    fun importEncryptedBackup(
        code: String,
        passphrase: String,
    ): DashboardState {
        val normalizedCode = code.trim()
        val normalizedPassphrase = passphrase.trim()
        require(normalizedCode.isNotEmpty()) { "Paste an encrypted backup first." }
        require(normalizedPassphrase.length >= 8) {
            "Use the same backup passphrase with at least 8 characters."
        }

        val envelope = exportJson.decodeFromString<EncryptedBackupEnvelope>(normalizedCode)
        val payloadJson =
            decrypt(
                cipherText = envelope.cipherTextBase64.fromBase64(),
                passphrase = normalizedPassphrase,
                salt = envelope.saltBase64.fromBase64(),
                iv = envelope.ivBase64.fromBase64(),
            ).decodeToString()
        val payload = exportJson.decodeFromString<ExportPayload>(payloadJson)
        return payload.toDashboardState()
    }

    fun createHouseholdShareCode(state: DashboardState): String {
        val packet =
            HouseholdSharePacket(
                generatedAtIso = Instant.now().toString(),
                planningData = state.planningData,
                schedules = state.schedules,
                checklist = state.checklist,
            )
        return exportJson.encodeToString(packet).encodeToByteArray().toBase64()
    }

    fun importHouseholdShareCode(
        code: String,
        currentState: DashboardState,
    ): DashboardState {
        val normalizedCode = code.trim()
        require(normalizedCode.isNotEmpty()) { "Paste a household share code first." }
        val packet =
            exportJson.decodeFromString<HouseholdSharePacket>(
                normalizedCode.fromBase64().decodeToString(),
            )
        return currentState.copy(
            planningData = packet.planningData,
            schedules = packet.schedules,
            checklist = packet.checklist,
        )
    }
}

private fun ExportPayload.toDashboardState(): DashboardState {
    return DashboardState(
        settings = settings,
        year = year,
        observances = observancesFor(year, settings),
        statusesById = statusesById,
        fridayNotesById = fridayNotesById,
        planningData = planningData,
        schedules = schedules,
        intermittentSessions = intermittentSessions,
        activeIntermittentFast = activeIntermittentFast,
        intermittentPresetHours = boundedPresetHours(intermittentPresetHours),
        profiles = profiles,
        reflections = reflections,
        checklist = checklist,
        premiumCompanionState = premiumCompanionState,
        launchFunnelSnapshot = launchFunnelSnapshot,
    )
}

private fun encrypt(
    plainText: ByteArray,
    passphrase: String,
    salt: ByteArray,
    iv: ByteArray,
): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, deriveKey(passphrase, salt), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
    return cipher.doFinal(plainText)
}

private fun decrypt(
    cipherText: ByteArray,
    passphrase: String,
    salt: ByteArray,
    iv: ByteArray,
): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, deriveKey(passphrase, salt), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
    return cipher.doFinal(cipherText)
}

private fun deriveKey(
    passphrase: String,
    salt: ByteArray,
): SecretKeySpec {
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val keySpec: KeySpec = PBEKeySpec(passphrase.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_LENGTH_BITS)
    val secret = factory.generateSecret(keySpec)
    return SecretKeySpec(secret.encoded, "AES")
}

private fun randomBytes(length: Int): ByteArray = ByteArray(length).also(SecureRandom()::nextBytes)

private fun ByteArray.toBase64(): String = Base64.getEncoder().encodeToString(this)

private fun String.fromBase64(): ByteArray = Base64.getDecoder().decode(this)
