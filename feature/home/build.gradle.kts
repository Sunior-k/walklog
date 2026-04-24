import com.river.walklog.setNamespace
plugins {
    id("river.android.feature")
    id("river.android.uitest")
}

android {
    setNamespace("feature.home")
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)
    implementation(projects.core.analytics)
    implementation(projects.core.native)
    implementation(libs.androidx.health.connect)
}
