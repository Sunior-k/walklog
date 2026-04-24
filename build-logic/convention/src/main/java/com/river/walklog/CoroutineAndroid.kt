package com.river.walklog

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureCoroutineAndroid() {
    configureCoroutineKotlin()
    dependencies {
        "implementation"(findLibrary("coroutines.android"))
    }
}

internal fun Project.configureCoroutineKotlin() {
    dependencies {
        "implementation"(findLibrary("coroutines.core"))
        "testImplementation"(findLibrary("coroutines.test"))
    }
}