plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
                }
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.multiplatform.settings)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            // Koin core for DI
            implementation(libs.koin.core)
            
            // Compose dependencies for KMP
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            api(compose.components.resources)
            implementation(compose.materialIconsExtended)
        }

        androidMain.dependencies {
            // Android specific dependencies
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.mlkit.digital.ink)
            implementation(libs.play.services.games)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Configuration standard avec la nouvelle version du plugin
compose.resources {
    publicResClass = true
    packageOfResClass = "org.nihongo.mochi.shared.generated.resources"
}

android {
    namespace = "org.nihongo.mochi.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
