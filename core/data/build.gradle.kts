import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.test")
}

android {
    setNamespace("core.data")
}

dependencies {
    api(projects.core.database)
    api(projects.core.datastore)
    api(projects.core.model)
    implementation(projects.core.network)
    implementation(projects.core.common)
    implementation(libs.coroutines.android)
    implementation(libs.androidx.health.connect)
}
