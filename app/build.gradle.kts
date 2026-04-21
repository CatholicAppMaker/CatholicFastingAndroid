import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val releaseKeystoreProperties =
    Properties().apply {
        val propertiesFile = rootProject.file("keystore.properties")
        if (propertiesFile.exists()) {
            propertiesFile.inputStream().use(::load)
        }
    }

val hasReleaseSigning =
    listOf("storeFile", "storePassword", "keyAlias", "keyPassword").all { key ->
        !releaseKeystoreProperties.getProperty(key).isNullOrBlank()
    }

android {
    namespace = "com.kevpierce.catholicfastingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kevpierce.catholicfastingapp"
        minSdk = 27
        targetSdk = 35
        versionCode = 10001
        versionName = "1.0.0"

        testInstrumentationRunner = "com.kevpierce.catholicfastingapp.ReleaseAndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        vectorDrawables.useSupportLibrary = true
    }

    testOptions {
        animationsDisabled = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(checkNotNull(releaseKeystoreProperties.getProperty("storeFile")))
                storePassword = checkNotNull(releaseKeystoreProperties.getProperty("storePassword"))
                keyAlias = checkNotNull(releaseKeystoreProperties.getProperty("keyAlias"))
                keyPassword = checkNotNull(releaseKeystoreProperties.getProperty("keyPassword"))
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
                debugSymbolLevel = "FULL"
            }
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:rules"))
    implementation(project(":core:data"))
    implementation(project(":core:billing"))
    implementation(project(":core:ui"))
    implementation(project(":core:widget"))
    implementation(project(":feature:today"))
    implementation(project(":feature:calendar"))
    implementation(project(":feature:tracker"))
    implementation(project(":feature:guidance"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:premium"))

    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.billing.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.junit4)
    testImplementation(libs.truth)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation(libs.truth)
    androidTestUtil(libs.androidx.test.orchestrator)
}
