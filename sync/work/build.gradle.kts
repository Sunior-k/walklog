import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.hilt")
}

android {
    setNamespace("sync.work")
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.common)

    implementation(libs.androidx.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)
}
