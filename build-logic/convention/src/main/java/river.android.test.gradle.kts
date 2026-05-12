import com.river.walklog.configureAndroidJacocoReport
import com.river.walklog.configureAndroidTestOptions
import com.river.walklog.findLibrary

configureAndroidTestOptions()
configureAndroidJacocoReport()

dependencies {
    "testImplementation"(findLibrary("junit4").get())
    "testImplementation"(findLibrary("mockk").get())
    "testImplementation"(findLibrary("turbine").get())
    "testImplementation"(findLibrary("coroutines-test").get())
    "testImplementation"(findLibrary("kotlin-test").get())
    "androidTestImplementation"(findLibrary("androidx-runner").get())
}
