package com.river.walklog

import org.gradle.api.Project

fun Project.setNamespace(name: String) {
    androidExtension.apply {
        namespace = "com.river.walklog.$name"
    }
}