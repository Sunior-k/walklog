import com.river.walklog.findLibrary

plugins {
    id("river.android.library")
    id("river.android.hilt")
    id("river.android.compose")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(findLibrary("hilt.navigation.compose").get())
    implementation(findLibrary("kotlinx.serialization.json").get())
    implementation(findLibrary("androidx.compose.navigation").get())
    implementation(findLibrary("androidx.lifecycle.viewModelCompose").get())
    implementation(findLibrary("androidx.lifecycle.runtimeCompose").get())
    androidTestImplementation(findLibrary("androidx.compose.ui.test").get())
    debugImplementation(findLibrary("androidx.compose.ui.test.manifest").get())
}
