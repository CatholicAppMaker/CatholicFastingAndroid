plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.kevpierce.catholicfasting.core.ui"
    compileSdk = 37
    ndkVersion = "29.0.14206865"

    defaultConfig { minSdk = 27 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.junit4)
    testImplementation(libs.truth)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.robolectric)
}

val verifySacredArtwork by
    tasks.registering {
        group = "verification"
        description = "Verifies the optimized sacred artwork resource budget."
        val artworkDirectory = layout.projectDirectory.dir("src/main/res/drawable-nodpi")
        inputs.dir(artworkDirectory)

        doLast {
            val artworkFiles =
                artworkDirectory.asFile
                    .listFiles()
                    ?.toList()
                    .orEmpty()
            val expectedStems =
                setOf(
                    "guidance_sacred",
                    "hero_sacred",
                    "sacred_advent_wreath",
                    "sacred_almsgiving_table",
                    "sacred_ash_wednesday",
                    "sacred_cathedral_light",
                    "sacred_chalice_vine",
                    "sacred_chi_rho",
                    "sacred_crucifix_altar",
                    "sacred_desert_pilgrimage",
                    "sacred_ember_days",
                    "sacred_friday_abstinence",
                    "sacred_jerusalem_cross",
                    "sacred_lenten_path",
                    "sacred_marian_monogram",
                    "sacred_monstrance",
                    "sacred_palm_sunday",
                    "sacred_paschal_candle",
                    "sacred_planning_journal",
                    "sacred_purple_veil",
                    "sacred_rosary_cross",
                    "sacred_sacred_heart",
                    "sacred_scripture_candle",
                )
            val legacyFiles =
                artworkFiles.filter { file ->
                    file.extension.lowercase() in setOf("png", "jpg", "jpeg")
                }
            check(legacyFiles.isEmpty()) {
                "Legacy sacred artwork must be WebP: ${legacyFiles.joinToString { it.name }}"
            }

            val webpFiles = artworkFiles.filter { it.extension.equals("webp", ignoreCase = true) }
            val actualStems = webpFiles.map { it.nameWithoutExtension }.toSet()
            check(actualStems == expectedStems) {
                "Sacred WebP set differs. Missing=${expectedStems - actualStems}; unexpected=${actualStems - expectedStems}."
            }
            val oversizedFiles = webpFiles.filter { it.length() > 200L * 1024L }
            check(oversizedFiles.isEmpty()) {
                "Sacred artwork exceeds 200 KiB: ${oversizedFiles.joinToString { it.name }}"
            }
            val totalBytes = webpFiles.sumOf { it.length() }
            check(totalBytes <= 2L * 1024L * 1024L) {
                "Sacred artwork exceeds 2 MiB: $totalBytes bytes."
            }
        }
    }

tasks.named("check").configure {
    dependsOn(verifySacredArtwork)
}

tasks.named("preBuild").configure {
    dependsOn(verifySacredArtwork)
}
