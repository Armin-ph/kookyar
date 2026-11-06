import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// ---------------------------------------------------------------------------
// Signing (امضای نسخهٔ انتشار):
// فایل keystore.properties (که به git اضافه نمی‌شود) می‌تواند در یکی از این
// دو مسیر قرار بگیرد — هر دو پشتیبانی می‌شوند:
//   1) ریشهٔ پروژه:      kookyar/keystore.properties   (پیشنهادی)
//   2) کنار همین فایل:   kookyar/app/keystore.properties
//
// محتوای فایل:
//   storeFile=/absolute/path/to/release.keystore   (مسیر نسبی = نسبت به ریشهٔ پروژه)
//   storePassword=****
//   keyAlias=kookyar
//   keyPassword=****
//
// اگر این فایل وجود نداشته باشد، بیلدهای release «بدون امضا» ساخته می‌شوند
// و بیلدهای debug هیچ تغییری نمی‌کنند.
// ---------------------------------------------------------------------------

val keystorePropertiesFile = listOf(
    rootProject.file("keystore.properties"),
    rootProject.file("app/keystore.properties"),
).firstOrNull { it.exists() }

val keystoreProperties = Properties()
if (keystorePropertiesFile != null) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

android {
    namespace = "com.kookyar.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kookyar.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1.0"
        resourceConfigurations.addAll(listOf("fa", "en"))
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            keystorePropertiesFile?.let {
                storeFile = rootProject.file(
                    requireNotNull(keystoreProperties.getProperty("storeFile")) {
                        "keystore.properties: 'storeFile' is missing"
                    }
                )
                storePassword = requireNotNull(keystoreProperties.getProperty("storePassword")) {
                    "keystore.properties: 'storePassword' is missing"
                }
                keyAlias = requireNotNull(keystoreProperties.getProperty("keyAlias")) {
                    "keystore.properties: 'keyAlias' is missing"
                }
                keyPassword = requireNotNull(keystoreProperties.getProperty("keyPassword")) {
                    "keystore.properties: 'keyPassword' is missing"
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePropertiesFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
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
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")

    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
