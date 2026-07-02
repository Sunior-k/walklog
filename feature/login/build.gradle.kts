import com.river.walklog.setNamespace

plugins {
    id("river.android.feature")
    id("river.android.test")
}

android {
    setNamespace("feature.login")
}

dependencies {
    implementation(projects.core.auth)
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.core.analytics)
}
