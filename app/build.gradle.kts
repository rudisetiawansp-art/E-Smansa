import com.android.build.api.dsl.ApplicationExtension
import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.compose)
}

extensions.configure<ApplicationExtension> {
    namespace = "id.rudissp.esmansa"
    compileSdk = 37

    defaultConfig {
        applicationId = "id.rudissp.esmansa"
        minSdk = 26
        targetSdk = 37
        versionCode = 23
        versionName = "3.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isShrinkResources = true
            ndk {
                abiFilters.add("armeabi-v7a")
                abiFilters.add("arm64-v8a")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
    compileSdkMinor = 0
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // 1. Menggunakan Compose BOM versi aslimu agar tidak error
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.constraintlayout)

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // 2. Pemanggilan Material 3 versi 1.4.0 secara langsung agar LoadingIndicator bisa berjalan
    implementation("androidx.compose.material3:material3:1.4.0")

    implementation("com.google.android.material:material:1.14.0")

    // 3. Wajib ada untuk animasi perubahan bentuk (morphing) pada LoadingIndicator Expressive
    implementation("androidx.graphics:graphics-shapes:1.1.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Network & Image
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.github.bumptech.glide:glide:5.0.9")
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.7")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.messaging)

    // Lainnya
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("androidx.camera:camera-camera2:1.6.1")
    implementation("androidx.camera:camera-lifecycle:1.6.1")
    implementation("androidx.camera:camera-view:1.6.1")
    implementation("com.kizitonwose.calendar:view:2.4.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("androidx.compose.material:material-icons-extended:1.7.8")
}