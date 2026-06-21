plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "wombat.joshattic.us.wear"
    //noinspection GradleDependency
    compileSdk = 36

    defaultConfig {
        val phoneVersionCode = project(":app").android.defaultConfig.versionCode ?: 1
        versionCode = phoneVersionCode * 1000

        applicationId = "wombat.joshattic.us"
        minSdk = 30          // Wear OS 3+ required for Compose for Wear OS
        targetSdk = 36
        versionName = project(":app").android.defaultConfig.versionName ?: "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Wear OS Compose
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.navigation)

    // Android basics
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Compose foundation (needed for some Wear tooling)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)

    // Auth storage
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.gson)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)

    // Images (profile pictures only)
    implementation(libs.coil.compose)

    // Wearable Data Layer (receive auth token from phone)
    implementation(libs.play.services.wearable)

    // On-watch text input
    implementation(libs.wear.input)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
