pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "spring-boot2-kotlin"

include(
    "model",
    "core",
    "batch",
    "app-api",
    "internal-api"
)
