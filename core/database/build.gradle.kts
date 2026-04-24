import com.river.walklog.setNamespace
plugins {
    id("river.android.library")
    alias(libs.plugins.room)
}

android {
    setNamespace("core.database")

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    api(projects.core.model)

    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
