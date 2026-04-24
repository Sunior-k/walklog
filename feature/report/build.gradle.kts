import com.river.walklog.setNamespace
plugins {
    id("river.android.feature")
    id("river.android.uitest")
}

android {
    setNamespace("feature.report")
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.analytics)
}
