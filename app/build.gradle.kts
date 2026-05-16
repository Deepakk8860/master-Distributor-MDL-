plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.android.masterdistributormdl"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.android.masterdistributormdl"
        minSdk = 24
        targetSdk = 35
        versionCode = 14
        versionName = "1.0.13"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    // Enable data binding
    buildFeatures {
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }
    buildFeatures{
        buildConfig =true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    //retrofit
    implementation (libs.retrofit)
    implementation (libs.gson)
    implementation (libs.okhttp)
    implementation (libs.converter.gson)

    //cameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.video)
    implementation(libs.camerax.view)

    //dexter
    implementation(libs.dexter)

    implementation(libs.material)

    //playerView
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.dash)


    //viewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    //ok http
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)


    //glide
    androidTestImplementation(libs.androidx.espresso.core.v351)
    implementation (libs.glide)
    annotationProcessor(libs.compiler)



    //scalars
    implementation(libs.converter.scalars)

    //firebase messaging
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    //pushwoosh
    implementation(libs.pushwoosh.firebase)

    //firebase
    implementation(libs.appinvokesdk)

    //coil for image processing
    implementation(libs.coil)
    implementation(libs.coil.svg) // For SVG support

    //swipe refresh layout
    implementation(libs.androidx.swiperefreshlayout)

    //mlkit
    implementation(libs.play.services.mlkit.text.recognition)
    //piccasso
    implementation(libs.picasso)
    //google login
    implementation(libs.play.services.auth)
    implementation(libs.play.services.location)
    implementation (libs.android.pdf.viewer)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}