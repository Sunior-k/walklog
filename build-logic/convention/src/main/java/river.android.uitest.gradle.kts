import com.river.walklog.configureAndroidJacocoReport
import com.river.walklog.configureAndroidUiTestOptions
import com.river.walklog.findLibrary

configureAndroidUiTestOptions()
configureAndroidJacocoReport()

dependencies {
    "testImplementation"(findLibrary("robolectric").get())
    "testImplementation"(findLibrary("androidx-compose-ui-test").get())
    "androidTestImplementation"(findLibrary("androidx-runner").get())
    "debugImplementation"(findLibrary("androidx-compose-ui-test-manifest").get())
    "releaseImplementation"(findLibrary("androidx-compose-ui-test-manifest").get())
}
