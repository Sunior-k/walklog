pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        // reward-store-rn의 브라운필드 RN AAR(com.river.walklog:reactnativeapp)을 소비하기 위함.
        // `cd reward-store-rn && npx brownfield package:android && npx brownfield publish:android`로
        // ~/.m2/repository에 게시됨.
        mavenLocal()
    }
}

rootProject.name = "walklog"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":benchmark")

// Core modules
include(":core:native")
include(":core:common")
include(":core:model")
include(":core:domain")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:data")
include(":core:designsystem")
include(":core:analytics")
include(":core:auth")
include(":core:testing")
include(":core:ui")

// Sync modules
include(":sync:work")

// Feature modules
include(":feature:recap")
include(":feature:home")
include(":feature:mission")
include(":feature:report")
include(":feature:widget")
include(":feature:onboarding")
include(":feature:login")
include(":feature:settings")
include(":feature:history")
include(":feature:reward")
