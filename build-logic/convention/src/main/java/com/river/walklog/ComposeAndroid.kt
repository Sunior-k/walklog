package com.river.walklog

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun Project.configureAndroidCompose() {
    with(plugins) {
        apply("org.jetbrains.kotlin.plugin.compose")
        apply("org.jetbrains.kotlin.plugin.serialization")
    }

    androidExtension.apply {

        buildFeatures.apply {
            compose = true
        }

        dependencies {
            val bom = findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("implementation", findLibrary("androidx.compose.material3").get())
            add("implementation", findLibrary("androidx.compose.ui").get())
            add("implementation", findLibrary("androidx.compose.ui.tooling.preview").get())
            add("implementation", findLibrary("androidx.compose.foundation").get())
            add("debugImplementation", findLibrary("androidx.compose.ui.tooling").get())
        }
    }
}
