plugins {
    id("org.jetbrains.kotlin.jvm")
}

// Pure Kotlin module — deliberately NO Android dependencies.
// Every type here must be usable from ViewModels, repositories, and (later)
// a shared backend/server codebase without pulling in the Android SDK.

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
