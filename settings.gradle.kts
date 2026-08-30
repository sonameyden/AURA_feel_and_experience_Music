pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AURA"

include(":app")
include(":core:designsystem")
include(":core:model")
include(":core:data")
include(":core:audio")
include(":core:common")
include(":feature:home")
include(":feature:search")
include(":feature:moodinput")
include(":feature:nowplaying")
include(":feature:library")
include(":feature:playlist")
include(":feature:artist")
include(":feature:settings")
