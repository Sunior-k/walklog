package com.river.walklog

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

private const val JACOCO_VERSION = "0.8.14"

private val coverageExcludes = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "**/*\$InjectAdapter.*",
    "**/*\$ModuleAdapter.*",
    "**/*\$ViewInjector*.*",
    "**/*Hilt*.*",
    "**/Hilt_*.*",
    "**/*_HiltModules*.*",
    "**/*_Factory.*",
    "**/*_MembersInjector.*",
    "**/*_GeneratedInjector.*",
    "**/*_Provide*Factory.*",
    "**/*_Impl.*",
    "**/*ComposableSingletons*.*",
)

internal fun Project.configureJacoco() {
    pluginManager.apply("jacoco")

    extensions.configure<JacocoPluginExtension> {
        toolVersion = JACOCO_VERSION
    }

    tasks.withType<Test>().configureEach {
        failOnNoDiscoveredTests.set(false)

        extensions.configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }
}

internal fun Project.configureAndroidJacocoReport(variantName: String = "debug") {
    configureJacoco()

    val capitalizedVariantName = variantName.replaceFirstChar(Char::titlecase)
    val unitTestTaskName = "test${capitalizedVariantName}UnitTest"

    tasks.register<JacocoReport>("jacoco${capitalizedVariantName}Report") {
        group = "verification"
        description = "Generates JaCoCo coverage reports for the $variantName unit tests."

        dependsOn(unitTestTaskName)

        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
            xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/$variantName/jacoco.xml"))
            html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/$variantName/html"))
        }

        sourceDirectories.setFrom(
            files(
                "src/main/java",
                "src/main/kotlin",
            ),
        )
        classDirectories.setFrom(
            files(
                fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/$variantName")) {
                    exclude(coverageExcludes)
                },
                fileTree(layout.buildDirectory.dir("intermediates/javac/$variantName/classes")) {
                    exclude(coverageExcludes)
                },
            ),
        )
        executionData.setFrom(
            fileTree(layout.buildDirectory) {
                include(
                    "jacoco/$unitTestTaskName.exec",
                    "outputs/unit_test_code_coverage/${variantName}UnitTest/$unitTestTaskName.exec",
                    "outputs/code_coverage/**/*.ec",
                )
            },
        )

        onlyIf {
            executionData.files.any { it.exists() }
        }
    }
}

internal fun Project.configureKotlinJacocoReport() {
    configureJacoco()

    val reportTask = if (tasks.names.contains("jacocoTestReport")) {
        tasks.named<JacocoReport>("jacocoTestReport")
    } else {
        tasks.register<JacocoReport>("jacocoTestReport")
    }

    reportTask.configure {
        group = "verification"
        description = "Generates JaCoCo coverage reports for the unit tests."

        dependsOn("test")

        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
            xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/test/jacoco.xml"))
            html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/test/html"))
        }

        sourceDirectories.setFrom(
            files(
                "src/main/java",
                "src/main/kotlin",
            ),
        )
        classDirectories.setFrom(
            files(
                fileTree(layout.buildDirectory.dir("classes/java/main")) {
                    exclude(coverageExcludes)
                },
                fileTree(layout.buildDirectory.dir("classes/kotlin/main")) {
                    exclude(coverageExcludes)
                },
            ),
        )
        executionData.setFrom(
            fileTree(layout.buildDirectory) {
                include(
                    "jacoco/test.exec",
                    "jacoco/*.exec",
                )
            },
        )

        onlyIf {
            executionData.files.any { it.exists() }
        }
    }
}
