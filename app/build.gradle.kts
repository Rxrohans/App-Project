plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.testapp2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.testapp2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "LbpApp")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("androidx.room:room-runtime:2.3.0")
    implementation ("androidx.sqlite:sqlite:2.1.0")

    implementation ("androidx.work:work-runtime-ktx:2.7.1")

    implementation ("androidx.appcompat:appcompat:1.3.1")
    implementation ("androidx.core:core-ktx:1.6.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation ("androidx.room:room-ktx:2.3.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.google.android.material:material:1.4.0")

}