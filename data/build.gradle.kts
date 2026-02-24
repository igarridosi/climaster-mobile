plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp) // Room-entzat ezinbestekoa
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.climaster.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        // Room-ek datu-basearen eskema non gorde behar duen jakiteko
        //noinspection WrongGradleMethod
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Barne Moduluak
    implementation(project(":core"))
    implementation(project(":domain")) // Data geruzak Domeinua ezagutu BEHAR du (interfazeak inplementatzeko)

    // Retrofit (Sarea)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Room (Datu-basea)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler) // KSP erabiltzen dugu KAPT ordez (azkarragoa)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
}

hilt {
    enableAggregatingTask = false
}
