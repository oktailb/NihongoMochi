plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
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
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.multiplatform.settings)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Task to copy common resources to a generated assets directory for Android
val copyCommonResources = tasks.register<Copy>("copyCommonResources") {
    from("src/commonMain/resources")
    into(layout.buildDirectory.dir("generated/assets/common"))
}

// Ensure the copy happens before Android resources are processed
tasks.named("preBuild").configure {
    dependsOn(copyCommonResources)
}

android {
    namespace = "org.nihongo.mochi.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    
    sourceSets {
        getByName("main") {
            // Register the output folder of the task as an asset source
            assets.srcDir(copyCommonResources.map { it.destinationDir })
        }
    }
}
