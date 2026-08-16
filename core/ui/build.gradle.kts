import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.compose")
    id("river.android.hilt")
    id("river.android.test")
}

android {
    setNamespace("core.ui")
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.core.analytics)
    testImplementation(projects.core.testing)
}
