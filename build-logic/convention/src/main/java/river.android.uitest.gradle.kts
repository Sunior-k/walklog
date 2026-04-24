import com.river.walklog.configureAndroidUiTestOptions
import com.river.walklog.findLibrary

configureAndroidUiTestOptions()

dependencies {
    "testImplementation"(findLibrary("junit4").get())
    "testImplementation"(findLibrary("robolectric").get())
    "testImplementation"(findLibrary("mockk").get())
    "testImplementation"(findLibrary("coroutines-test").get())
    "testImplementation"(findLibrary("androidx-compose-ui-test").get())
    "debugImplementation"(findLibrary("androidx-compose-ui-test-manifest").get())
    "releaseImplementation"(findLibrary("androidx-compose-ui-test-manifest").get())
}
