plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.buttonball.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.buttonball.app"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    
    signingConfigs {
        create("release") {
            storeFile = file("../buttonball-release.jks")
            storePassword = "AkhilRana2012"
            keyAlias = "buttonball"
            keyPassword = "AkhilRana2012"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }
}
