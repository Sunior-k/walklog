package com.river.walklog

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureHiltAndroid() {
    with(pluginManager) {
        apply("dagger.hilt.android.plugin")
        apply("com.google.devtools.ksp")
    }

    dependencies {
        "implementation"(findLibrary("hilt.android"))
        "ksp"(findLibrary("hilt.android.compiler"))
    }
}