import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kover)
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

// Version is overridable from the environment so CI/release automation can stamp a unique,
// monotonically-increasing versionCode (e.g. from the CI run number or a release tag) instead of
// relying on a human to bump a hardcoded literal. Unset -> baseline; but a value that is *set yet
// invalid* fails the build loudly rather than silently shipping/colliding the baseline `1`.
val appVersionCode = System.getenv("DAYSSINCE_VERSION_CODE")?.let { raw ->
    raw.toIntOrNull()?.takeIf { it > 0 }
        ?: error("DAYSSINCE_VERSION_CODE must be a positive integer, but was: '$raw'")
} ?: 1
val appVersionName = System.getenv("DAYSSINCE_VERSION_NAME")?.let { raw ->
    raw.ifBlank { error("DAYSSINCE_VERSION_NAME is set but blank") }
} ?: "1.0.0"

val signingValues = listOf(
    signingStoreFile, signingStorePassword, signingKeyAlias, signingKeyPassword,
)
val hasReleaseSigning = signingValues.all { !it.isNullOrBlank() }
val hasPartialReleaseSigning =
    !hasReleaseSigning && signingValues.any { !it.isNullOrBlank() }

if (hasPartialReleaseSigning) {
    // Surface this loudly — a partial config is almost always a misconfiguration
    // (typo in a property name, missing env var on CI, etc.) and the silent
    // fallback to an unsigned APK only gets caught at upload time.
    logger.warn(
        "DaysSince: partial release signing config detected. " +
            "Some of [storeFile, storePassword, keyAlias, keyPassword] are set " +
            "but not all — :app:assembleRelease will produce an UNSIGNED APK. " +
            "Check keystore.properties or the DAYSSINCE_KEYSTORE_* / DAYSSINCE_KEY_* " +
            "environment variables.",
    )
}

android {
    namespace = "com.quasarapps.dayssince"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.quasarapps.dayssince"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

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

    // composeOptions { kotlinCompilerExtensionVersion } is no longer needed: the Compose compiler
    // is applied as the org.jetbrains.kotlin.plugin.compose Gradle plugin (Kotlin 2.0+).

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }

        // Gradle Managed Device: AGP provisions/boots/tears down the emulator, so the instrumentation
        // suite runs with the same `./gradlew :app:pixel2api30DebugAndroidTest` command locally and in
        // CI. `aosp-atd` is an Automated Test Device image — headless- and CI-optimised, and matches
        // the app (no Google Play Services dependency).
        managedDevices {
            localDevices {
                create("pixel2api30") {
                    device = "Pixel 2"
                    apiLevel = 30
                    systemImageSource = "aosp-atd"
                }
            }
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

    // Periodic background refresh for placed widgets.
    implementation(libs.androidx.work.runtime.ktx)

    debugImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)

    // Compose UI tests run on the JVM under Robolectric (no emulator needed).
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // On-device test infrastructure: ApplicationProvider, ActivityScenario(Rule), runner/rules.
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)

    // WorkManager test helpers (TestListenableWorkerBuilder) for the widget refresh worker.
    androidTestImplementation(libs.androidx.work.testing)

    // Coroutine test helpers (runTest, test dispatchers) for repository / view-model tests.
    androidTestImplementation(libs.kotlinx.coroutines.test)

    // Navigation under test (TestNavHostController) for the end-to-end app navigation test.
    androidTestImplementation(libs.androidx.navigation.testing)

    // Glance widget composable unit testing (runGlanceAppWidgetUnitTest).
    androidTestImplementation(libs.androidx.glance.testing)
    androidTestImplementation(libs.androidx.glance.appwidget.testing)

    // Compose UI tests on a device/emulator. The stub ComponentActivity that createComposeRule()
    // hosts content in comes from the debugImplementation(ui-test-manifest) above, merged into the
    // app-under-test's debug manifest, so it does not need repeating here.
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}