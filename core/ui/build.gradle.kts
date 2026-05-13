import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.compose")
}

android {
    setNamespace("core.ui")
}

dependencies {
    api(projects.core.model)
}
