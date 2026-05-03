package com.river.walklog

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureHiltKotlin() {
    with(pluginManager) {
        apply("com.google.devtools.ksp")
    }

    dependencies {
        "implementation"(findLibrary("hilt.core"))
        "ksp"(findLibrary("hilt.compiler"))
    }
}