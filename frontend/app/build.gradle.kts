plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // coroutines
    implementation(libs.kotlinx.coroutines.core)

    //fragment
    implementation (libs.androidx.fragment.ktx)

    //view model
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v270)
    implementation(libs.androidx.lifecycle.livedata.core.ktx)
    implementation(libs.androidx.activity.ktx)

    // room db (local storage)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

    //connect to API (retrofit)
    implementation (libs.retrofit)
    implementation (libs.converter.moshi)
    implementation (libs.okhttp)
    implementation (libs.logging.interceptor)
    implementation (libs.moshi)
    implementation (libs.moshi.kotlin)
    kapt(libs.moshi.kotlin.codegen)
    implementation (libs.picasso)
}