import com.river.walklog.configureHiltAndroid
import com.river.walklog.configureKotlinAndroid

plugins {
    id("com.android.library")
}

configureKotlinAndroid()
configureHiltAndroid()