plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sqldelight)
}

kotlin {
    // Enregistrement de la cible Android
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            api(compose.components.resources)
            implementation(compose.materialIconsExtended)
            
            implementation(libs.rich.editor)
            implementation(libs.jetbrains.navigation.compose)

            // SQLDelight
            implementation(libs.sqldelight.coroutines.extensions)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.mlkit.digital.ink)
            implementation(libs.play.services.games)
            // Correction ici : tirets remplacés par des points
            implementation(libs.sqldelight.android.driver)
        }

        iosMain.dependencies {
            // Correction ici : tirets remplacés par des points
            implementation(libs.sqldelight.native.driver)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

sqldelight {
    databases {
        create("MochiDatabase") {
            packageName.set("org.nihongo.mochi.db")
        }
    }
}

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
