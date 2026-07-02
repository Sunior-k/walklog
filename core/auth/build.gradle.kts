import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.hilt")
}

android {
    setNamespace("core.auth")
}

dependencies {
    api(projects.core.model)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
}
