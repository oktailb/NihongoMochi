import java.util.Properties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.github.jk1.dependency-license-report") version "2.9"
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "org.nihongo.mochi"
    compileSdk = 36

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
    }

    defaultConfig {
        applicationId = "org.nihongo.mochi"
        minSdk = 28
        targetSdk = 36
        versionCode = 30
        versionName = "0.9.6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val buildDate = SimpleDateFormat("dd MMM. yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        buildConfigField("String", "BUILD_DATE", "\"$buildDate\"")
    }

    sourceSets {
        getByName("main") {
            // Re-include shared resources as Android assets for AndroidAudioPlayer
            assets.srcDirs("${rootProject.rootDir}/shared/src/commonMain/composeResources/files")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            packaging { jniLibs { useLegacyPackaging = true } }
        }
        debug {
            packaging { jniLibs { useLegacyPackaging = true } }
        }
    }
    
    if (!gradle.startParameter.taskNames.any { it.contains("bundleRelease", ignoreCase = true) }) {
        splits {
            abi {
                isEnable = true
                reset()
                include("x86_64", "arm64-v8a")
                isUniversalApk = false
            }
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlin {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    }
    
    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
}

licenseReport {
    renderers = arrayOf(com.github.jk1.license.render.JsonReportRenderer("licenses.json"))
}

// Copy to a GENERATED directory in shared, not into src/
val copyLicensesToSharedResources = tasks.register<Copy>("copyLicensesToSharedResources") {
    from(layout.buildDirectory.file("reports/dependency-license/licenses.json"))
    into(rootProject.file("shared/build/generated/licenses/commonMain/composeResources/files"))
    dependsOn("generateLicenseReport")
}

// Link to build and ensure order for shared tasks
tasks.withType<com.android.build.gradle.tasks.MergeSourceSetFolders>().configureEach {
    dependsOn(copyLicensesToSharedResources)
}

// Force shared resource tasks to run after our copy to avoid implicit dependency issues
project(":shared").tasks.matching { it.name.startsWith("copyNonXmlValueResources") }.configureEach {
    mustRunAfter(copyLicensesToSharedResources)
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.games)
    implementation(libs.mlkit.digital.ink)
    implementation(libs.flexbox)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.multiplatform.settings)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.android.gms:play-services-oss-licenses:17.1.0")
}
