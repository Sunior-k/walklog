import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.test")
}

android {
    setNamespace("core.domain")
}

dependencies {
    api(projects.core.data)
    api(projects.core.model)
}
