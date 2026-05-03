import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.hilt")
}

android {
    setNamespace("core.datastore")
}

dependencies {
    api(projects.core.model)
    implementation(libs.androidx.datastore)
}
