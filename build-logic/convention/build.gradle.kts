plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidHilt") {
            id = "river.android.hilt"
            implementationClass = "com.river.walklog.HiltAndroidPlugin"
        }
        register("kotlinHilt") {
            id = "river.kotlin.hilt"
            implementationClass = "com.river.walklog.HiltKotlinPlugin"
        }
    }
}