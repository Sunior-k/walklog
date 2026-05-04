import com.river.walklog.setNamespace
plugins {
    id("river.android.feature")
    id("river.android.uitest")
}

android {
    setNamespace("feature.mission")
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.analytics)
    implementation(projects.core.native)
    implementation(libs.androidx.adaptive)
}
