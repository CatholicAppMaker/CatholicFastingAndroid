pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CatholicFastingAndroid"

include(
    ":app",
    ":core:model",
    ":core:rules",
    ":core:data",
    ":core:billing",
    ":core:ui",
    ":core:widget",
    ":feature:today",
    ":feature:calendar",
    ":feature:tracker",
    ":feature:guidance",
    ":feature:settings",
    ":feature:premium",
)
