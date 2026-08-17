import com.river.walklog.setNamespace

plugins {
    id("river.android.feature")
    id("river.android.uitest")
}

android {
    setNamespace("feature.reward")
}

dependencies {
    implementation(projects.core.analytics)
    implementation(projects.core.domain)
    implementation(projects.core.ui)
    testImplementation(projects.core.testing)
}
