import com.river.walklog.setNamespace
plugins {
    id("river.android.feature")
    id("river.android.uitest")
}

android {
    setNamespace("feature.mission")
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.analytics)
    implementation(projects.core.ui)
    implementation(libs.androidx.adaptive)
}
