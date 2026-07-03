import com.river.walklog.setNamespace

plugins {
    id("river.android.feature")
    id("river.android.uitest")
}

android {
    setNamespace("feature.onboarding")
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.ui)
    implementation(projects.core.auth)
    implementation(projects.core.domain)
    implementation(projects.core.analytics)
    implementation(libs.androidx.health.connect)
    testImplementation(projects.core.testing)
}
