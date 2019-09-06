import org.jetbrains.kotlin.gradle.tasks.*

val ideaActive = System.getProperty("idea.active") == "true"

val kotlin_version: String by extra
val ktor_version: String by extra
val coroutines_version: String by extra
val serialization_version: String by extra

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
}

kotlin {
    jvm()

    if (ideaActive) {
        iosX64("ios")
    }
    val iosArm32 = iosArm32("iosArm32")
    val iosArm64 = iosArm64("iosArm64")
    val iosX64 = iosX64("iosX64")

    val frameworkName = "KotlinConfAPI"

    configure(listOf(iosArm32, iosArm64, iosX64)) {
        compilations {
            val main by getting {
                extraOpts("-Xobjc-generics")
            }

        }

        binaries.framework {
            export("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutines_version")
            baseName = frameworkName
        }
    }

    tasks.register<FatFrameworkTask>("debugFatFramework") {
        baseName = frameworkName
        group = "Universal framework"
        description = "Builds a universal (fat) debug framework"

        from(iosX64.binaries.getFramework("DEBUG"))
    }

    tasks.register<FatFrameworkTask>("releaseFatFramework") {
        baseName = frameworkName
        group = "Universal framework"
        description = "Builds a universal (release) debug framework"

        from(iosArm64.binaries.getFramework("RELEASE"), iosArm32.binaries.getFramework("RELEASE"))
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutines_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serialization_version")

                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-serialization:$ktor_version")
            }
        }
        commonTest {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serialization_version")
                implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
                implementation("io.ktor:ktor-client-serialization-jvm:$ktor_version")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
            }
        }

        val iosMain = if (ideaActive) {
            getByName("iosMain")
        } else {
            create("iosMain")
        }

        iosMain.apply {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutines_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$serialization_version")

                implementation("io.ktor:ktor-client-ios:$ktor_version")
                implementation("io.ktor:ktor-client-serialization-native:$ktor_version")
            }
        }

        val iosArm32Main by getting
        val iosArm64Main by getting
        val iosX64Main by getting

        configure(listOf(iosArm32Main, iosArm64Main, iosX64Main)) {
            dependsOn(iosMain)
        }
    }
}