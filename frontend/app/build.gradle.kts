plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt") // Required for Room & Moshi Codegen
    id("com.google.gms.google-services") // For Firebase
    id("kotlin-parcelize") // Ditambahkan: Diperlukan untuk @Parcelize di data class
}

android {
    namespace = "com.example.mindfulu"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mindfulu"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.ktx)

    // Firebase
    // Ensure you are using the latest Firebase BOM. 33.0.0 is current as of June 2025.
    implementation(platform("com.google.firebase:firebase-bom:33.1.1")) // Gunakan versi BOM terbaru dari file libs.versions.toml Anda
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    // Ensure you are using the latest Play Services Auth. 21.0.0 is current as of June 2025.
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation(libs.firebase.firestore.ktx) // Tetap ada jika Anda berencana menggunakan Firestore

    // Credential Manager
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Material Design (Jika sudah ada di libs.material, yang ini mungkin duplikat. Biarkan dulu jika belum konflik)
    implementation ("com.google.android.material:material:1.12.0")

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    kapt(libs.moshi.kotlin.codegen)

    // Image
    implementation(libs.picasso)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Image Profile
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation("io.coil-kt:coil:2.5.0")

    // Testing
    testImplementation(libs.junit) // JUnit 4
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Mocking
    // For Mockito-Kotlin, which provides Kotlin-friendly APIs for Mockito
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1") // Or the latest version
    testImplementation("org.mockito:mockito-core:5.12.0") // Or the latest version
    // Required for mocking final classes/methods like some Firebase ones.
    // Also, create a file in `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
    // with content `mock-maker-inline` to enable this.
    testImplementation("org.mockito:mockito-inline:5.12.0")

    // Coroutine testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1") // Or the latest version

    // LiveData testing
    testImplementation("androidx.arch.core:core-testing:2.2.0") // Or the latest version
}