import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.compose")
    id("river.android.test")
}

android {
    setNamespace("core.ui")
}

dependencies {
    api(projects.core.model)
    testImplementation(projects.core.testing)
}
