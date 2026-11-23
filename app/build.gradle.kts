import com.android.build.api.variant.impl.VariantOutputImpl
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.drdisagree.glasslayer"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.drdisagree.glasslayer"
        minSdk = 36
        targetSdk = 37
        versionCode = 7
        versionName = "1.3.0"
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    var releaseSigning = signingConfigs.getByName("debug")

    try {
        val keystoreProperties = Properties()
        FileInputStream(keystorePropertiesFile).use { inputStream ->
            keystoreProperties.load(inputStream)
        }

        releaseSigning = signingConfigs.create("release") {
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
        }
    } catch (_: Exception) {
    }

    buildTypes {
        debug {
            signingConfig = releaseSigning
        }
        release {
            optimization {
                enable = true
            }
            signingConfig = releaseSigning
        }
        create("dev") {
            initWith(getByName("release"))
            optimization {
                enable = true
            }
            signingConfig = releaseSigning
            matchingFallbacks += listOf("release")
        }
        create("review") {
            initWith(getByName("release"))
            optimization {
                enable = true
            }
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }
    lint {
        checkReleaseBuilds = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            if (output is VariantOutputImpl) {
                output.outputFileName = "GlassLayer ${android.defaultConfig.versionName}.apk"
            }
        }
    }
}

tasks.register("printVersionName") {
    val versionName = android.defaultConfig.versionName
    doLast {
        println(versionName)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.libxposed.service)
    compileOnly(libs.libxposed.api)
}
