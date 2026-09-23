plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

import java.util.Properties
import java.io.FileInputStream

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
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
             if (keystorePropertiesFile.exists()) {
                 storeFile = file(keystoreProperties["storeFile"] as String)
                  storePassword = keystoreProperties["storePassword"] as String
                 keyAlias = keystoreProperties["keyAlias"] as String
                 keyPassword = keystoreProperties["keyPassword"] as String
             }
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
