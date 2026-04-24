import com.river.walklog.setNamespace

plugins {
    id("river.android.feature")
    id("river.android.test")
}

android {
    setNamespace("feature.onboarding")
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.analytics)
    implementation(libs.androidx.health.connect)
}
