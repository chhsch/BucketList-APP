// Top-level build.gradle.kts

plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("com.google.devtools.ksp") version "1.9.21-1.0.15" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.6" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
