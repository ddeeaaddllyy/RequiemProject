plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.application.requiemproject"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.application.requiemproject"
        minSdk = 31
        targetSdk = 36
        versionCode = providers.gradleProperty("versionCode").map { it.toInt() }.getOrElse(1)
        versionName = providers.gradleProperty("versionName").getOrElse("2.0.0")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
    }
    buildToolsVersion = "36.0.0"
    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
    }
}

dependencies {
    implementation(libs.koin.android)
    constraints {
        implementation("androidx.fragment:fragment:1.8.5") {
            because("ML Kit brings Fragment 1.0.0 transitively; Activity Result requires a compatible version")
        }
    }
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.text.recognition)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.savedstate.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}
