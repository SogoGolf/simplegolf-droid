import java.util.Properties

fun loadProperties(fileName: String): Properties {
    val props = Properties()
    val propFile = rootProject.file(fileName)
    if (propFile.exists()) {
        propFile.inputStream().use { props.load(it) }
    }
    return props
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    id("org.jetbrains.kotlin.kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.sogo.golf.msl"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sogo.golf.msl"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load properties and add to BuildConfig
        val prodProps = loadProperties("prod.properties")

        buildConfigField("String", "MSL_BASE_URL", "\"${prodProps["MSL_BASE_URL"] ?: "default-msl-base.com"}\"")
        buildConfigField("String", "MSL_BASE_URL_AUTH", "\"${prodProps["MSL_BASE_URL_AUTH"] ?: "default-auth.com"}\"")
        buildConfigField("String", "MSL_GOLF_URL", "\"${prodProps["MSL_GOLF_URL"] ?: "default-golf-api.com"}\"")
        buildConfigField("String", "MSL_GOLF_URL_V2", "\"${prodProps["MSL_GOLF_URL_V2"] ?: "default-golf-api-v2.com"}\"")
        buildConfigField("String", "MSL_AUTH_URL", "\"${prodProps["MSL_AUTH_URL"] ?: "default-id.com"}\"")
        buildConfigField("String", "MSL_URL", "\"${prodProps["MSL_URL"] ?: "default-msl.com"}\"")
        buildConfigField("String", "MSL_COMPANY_CODE", "\"${prodProps["MSL_COMPANY_CODE"] ?: "0"}\"")

        buildConfigField("String", "MIXPANEL_API_SECRET", "\"${prodProps["MIXPANEL_API_SECRET"] ?: "default-mixpanel"}\"")
        buildConfigField("String", "AMPLITUDE_ANALYTICS", "\"${prodProps["AMPLITUDE_ANALYTICS"] ?: "default-amplitude"}\"")

        buildConfigField("String", "REALM_APP_ID", "\"${prodProps["REALM_APP_ID"] ?: "default-realm"}\"")
        buildConfigField("String", "MONGO_API_KEY", "\"${prodProps["MONGO_API_KEY"] ?: "default-mongo"}\"")

        buildConfigField("String", "SOGO_MSL_AUTH_URL", "\"${prodProps["SOGO_MSL_AUTH_URL"] ?: "default-sogo-auth.com"}\"")
        buildConfigField("String", "SOGO_OCP_SUBSCRIPTION_KEY", "\"${prodProps["SOGO_OCP_SUBSCRIPTION_KEY"] ?: "default-key"}\"")
        buildConfigField("String", "SOGO_AUTHORIZATION", "\"${prodProps["SOGO_AUTHORIZATION"] ?: "default-auth"}\"")
        buildConfigField("String", "SOGO_GCP_API", "\"${prodProps["SOGO_GCP_API"] ?: "default-gcp.com"}\"")
        buildConfigField("String", "SOGO_MONGO_API", "\"${prodProps["SOGO_MONGO_API"] ?: "default-gcp.com"}\"")

        buildConfigField("String", "REVENUECAT", "\"${prodProps["REVENUECAT"] ?: "default-rc"}\"")
        buildConfigField("String", "REVENUECAT_5_TOKEN", "\"${prodProps["REVENUECAT_5_TOKEN"] ?: "default-5"}\"")
        buildConfigField("String", "REVENUECAT_10_TOKEN", "\"${prodProps["REVENUECAT_10_TOKEN"] ?: "default-10"}\"")
        buildConfigField("String", "REVENUECAT_20_TOKEN", "\"${prodProps["REVENUECAT_20_TOKEN"] ?: "default-20"}\"")

        buildConfigField("String", "SENTRY_DSN", "\"${prodProps["SENTRY_DSN"] ?: "default-sentry"}\"")


        // Add Room schema export directory
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true  // Enable BuildConfig generation
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.material3)
    implementation(libs.core.splashscreen)

    implementation(libs.hilt.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.common)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.gson)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    implementation(libs.threetenabp)

    //force app updates
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)

    //RevenueCat
    implementation(libs.purchases)
    implementation(libs.purchases.ui)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    //Mixpanel
    implementation(libs.mixpanel.android)

    //Encrypted shared preferences
    implementation(libs.androidx.security.crypto)

    //JWT Decoder
    implementation(libs.jwtdecode)

    //Signatures
    implementation(libs.signature.pad)

    //Icons
    implementation(libs.icons.lucide)

    //Images
    implementation(libs.coil.compose)

    //Signatures
    implementation(libs.signature.pad)

    //Firebase (excluding Analytics)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.messaging.ktx)

    //Amplitude analytics
    implementation(libs.analytics.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}