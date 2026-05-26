plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.googleServices)

}

android {
    namespace = "swadha.collection.rental"
    compileSdk = 34

    defaultConfig {
        applicationId = "swadha.collection.rental"
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
}

dependencies {


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(libs.volley)

    implementation("com.google.code.gson:gson:2.10.1")

    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.firestore)

    implementation(libs.firebase.auth)
    implementation(libs.gridlayout)

    testImplementation(libs.junit)

    androidTestImplementation(libs.ext.junit)

    androidTestImplementation(libs.espresso.core)

}