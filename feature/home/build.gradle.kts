import com.river.walklog.setNamespace
plugins {
    id("river.android.feature")
    id("river.android.uitest")
}

android {
    setNamespace("feature.home")
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.domain)
    implementation(projects.core.analytics)
    implementation(libs.androidx.health.connect)
    implementation(libs.androidx.adaptive)
}
