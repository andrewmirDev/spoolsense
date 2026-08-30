import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
 }

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()

    androidLibrary {
       namespace = "com.spoolsense.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.client.android)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.koin.android)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.websockets)
            implementation(libs.sqldelight.runtime)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose)
            api(libs.koin.core)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
        }

        androidUnitTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)

        }

        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.mockk)
        }

        jvmTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.mockk)
        }

        iosTest.dependencies {
            implementation(libs.sqldelight.native.driver)
        }

        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }

    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)

}

sqldelight {
    databases {
        create("SpoolDatabase") {
            packageName.set("com.spoolsense.shared.database")
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
    }
}