import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

// Release signing: loaded from a gitignored `keystore.properties` at the repo root
// (preferred for local builds) or from environment variables (preferred for CI).
// If neither is present, assembleRelease still works but produces an unsigned APK —
// fine for CI smoke-tests, not uploadable to Play.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

fun signingValue(propKey: String, envKey: String): String? =
    keystoreProperties.getProperty(propKey) ?: System.getenv(envKey)

val signingStoreFile = signingValue("storeFile", "DAYSSINCE_KEYSTORE_PATH")
val signingStorePassword = signingValue("storePassword", "DAYSSINCE_KEYSTORE_PASSWORD")
val signingKeyAlias = signingValue("keyAlias", "DAYSSINCE_KEY_ALIAS")
val signingKeyPassword = signingValue("keyPassword", "DAYSSINCE_KEY_PASSWORD")

val hasReleaseSigning = listOf(
    signingStoreFile, signingStorePassword, signingKeyAlias, signingKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "com.quasarapps.dayssince"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.quasarapps.dayssince"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(signingStoreFile!!)
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            // Let a debug build coexist with an installed release build on the same device,
            // and show up as "Days Since (debug)" on the launcher so it's obvious which is
            // which during development. The release variant resolves `${appLabel}` to
            // `@string/app_name`, so translations in values-*/strings.xml still apply.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            manifestPlaceholders["appLabel"] = "Days Since (debug)"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["appLabel"] = "@string/app_name"
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // Bumped to a Kotlin 1.9.x compatible Compose compiler version
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Navigation + lifecycle (multi-counter screens)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Persistence (milestones + widget bindings)
    implementation(libs.androidx.datastore.preferences)

    // Home-screen widgets (Glance / Compose)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    debugImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}