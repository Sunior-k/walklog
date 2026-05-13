enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "walklog-build-logic"

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
include(":convention")
