import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.hilt")
    id("river.android.test")
}

android {
    setNamespace("core.data")
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.native)
    implementation(projects.core.network)
    implementation(projects.core.common)
    implementation(libs.coroutines.android)
    implementation(libs.androidx.health.connect)
    implementation(libs.play.services.location)

    // Firestore
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(projects.core.testing)
}
