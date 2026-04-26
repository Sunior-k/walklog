package com.river.walklog

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * XML base + Compose 혼용 구조.
 *
 * - [FragmentContainerView] + Navigation Component(XML navGraph)로 화면 전환을 관리.
 * - Compose 화면은 각 Fragment에서 [ComposeView]로 감싸 그대로 사용.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    @Inject
    lateinit var crashReporter: CrashReporter

    private val bottomNavDestinations = setOf(
        R.id.homeFragment,
        R.id.historyFragment,
        R.id.rewardFragment,
        R.id.settingsFragment,
    )
    private var navController: NavController? = null
    private var isStartupReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !isStartupReady }
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val initialSettings = userSettingsRepository.settings.first()
            applyThemeMode(initialSettings.themeMode)
            setupContent(
                savedInstanceState = savedInstanceState,
                initialSettings = initialSettings,
            )
            observeThemeMode()
            isStartupReady = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        navController
            ?.currentDestination
            ?.id
            ?.also { destinationId ->
                outState.putInt(KEY_CURRENT_DESTINATION_ID, destinationId)
            }
        super.onSaveInstanceState(outState)
    }

    private fun setupContent(
        savedInstanceState: Bundle?,
        initialSettings: UserSettings,
    ) {
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        applySystemBarAppearance()
        setupNavigation(
            savedInstanceState = savedInstanceState,
            initialSettings = initialSettings,
        )
    }

    private fun setupNavigation(
        savedInstanceState: Bundle?,
        initialSettings: UserSettings,
    ) {
        val restoredDestination = savedInstanceState.restoredDestinationId()
        val firstLaunchDestination = resolveFirstLaunchDestination(
            savedInstanceState = savedInstanceState,
            initialSettings = initialSettings,
        )
        val graphStartDestination = resolveGraphStartDestination(
            firstLaunchDestination = firstLaunchDestination,
            restoredDestination = restoredDestination,
        )

        val navController = findNavController()
        this.navController = navController
        setStartDestination(navController, graphStartDestination)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bindBottomNavigation(bottomNav, navController)
        applyBottomNavigationInsets(bottomNav)
        syncBottomNavigationWithDestination(bottomNav, navController)
        restoreVisibleDestinationAfterRecreation(
            bottomNav = bottomNav,
            navController = navController,
            restoredDestination = restoredDestination,
        )
    }

    private fun findNavController(): NavController {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHost.navController
    }

    // BottomNav popUpTo 기준이 흔들리지 않도록 그래프 시작점과 복원 탭을 분리.
    private fun resolveGraphStartDestination(
        firstLaunchDestination: Int?,
        restoredDestination: Int?,
    ): Int =
        firstLaunchDestination
            ?: if (restoredDestination == R.id.onboardingFragment) {
                R.id.onboardingFragment
            } else {
                R.id.homeFragment
            }

    private fun resolveFirstLaunchDestination(
        savedInstanceState: Bundle?,
        initialSettings: UserSettings,
    ): Int? {
        if (savedInstanceState != null) return null
        return if (initialSettings.isOnboardingCompleted) {
            R.id.homeFragment
        } else {
            R.id.onboardingFragment
        }
    }

    private fun Bundle?.restoredDestinationId(): Int? =
        this?.getInt(KEY_CURRENT_DESTINATION_ID)?.takeIf { it != 0 }

    // 요청된 시작점으로 NavGraph 재설정
    private fun setStartDestination(
        navController: NavController,
        destinationId: Int,
    ) {
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(destinationId)
        navController.setGraph(graph, null)
    }

    private fun syncBottomNavigationWithDestination(
        bottomNav: BottomNavigationView,
        navController: NavController,
    ) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            syncBottomNavigationState(bottomNav, destination.id)
        }
        syncBottomNavigationState(bottomNav, navController.currentDestination?.id)
    }

    private fun restoreVisibleDestinationAfterRecreation(
        bottomNav: BottomNavigationView,
        navController: NavController,
        restoredDestination: Int?,
    ) {
        bottomNav.post {
            if (
                restoredDestination != null &&
                restoredDestination != navController.currentDestination?.id
            ) {
                navigateToRestoredDestination(navController, restoredDestination)
            }
            syncBottomNavigationState(bottomNav, navController.currentDestination?.id)
        }
    }

    // 테마 재생성과 탭 복원 모두 같은 옵션으로 이동하도록 BottomNav 처리.
    private fun bindBottomNavigation(
        bottomNav: BottomNavigationView,
        navController: NavController,
    ) {
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId in bottomNavDestinations) {
                navigateToBottomNavDestination(navController, item.itemId)
            } else {
                false
            }
        }
        bottomNav.setOnItemReselectedListener { item ->
            if (item.itemId in bottomNavDestinations) {
                navigateToBottomNavDestination(navController, item.itemId)
            }
        }
    }

    // 그래프 변경이나 복원 이동 후 현재 destination을 BottomNav에 반영.
    private fun syncBottomNavigationState(
        bottomNav: BottomNavigationView,
        destinationId: Int?,
    ) {
        bottomNav.isVisible = destinationId in bottomNavDestinations
        if (destinationId in bottomNavDestinations) {
            bottomNav.menu.findItem(destinationId ?: return)?.isChecked = true
        }
    }

    // 각 탭의 백스택 상태를 보존하면서 루트 탭 사이 이동
    private fun navigateToBottomNavDestination(
        navController: NavController,
        destinationId: Int,
    ): Boolean {
        if (navController.currentDestination?.id == destinationId) {
            return true
        }
        val options = navOptions {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        }
        return try {
            navController.navigate(destinationId, null, options)
            true
        } catch (e: IllegalArgumentException) {
            crashReporter.log("Bottom navigation failed: destinationId=$destinationId")
            crashReporter.recordException(e)
            false
        } catch (e: IllegalStateException) {
            crashReporter.log("Bottom navigation failed: destinationId=$destinationId")
            crashReporter.recordException(e)
            false
        }
    }

    // 그래프 시작점은 유지한 채 recreate 전 보이던 탭을 복원
    private fun navigateToRestoredDestination(
        navController: NavController,
        destinationId: Int,
    ) {
        navController.navigate(destinationId)
    }

    private fun applyBottomNavigationInsets(bottomNav: BottomNavigationView) {
        val initialBottomPadding = bottomNav.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { view, insets ->
            val navigationBarBottom = insets
                .getInsets(WindowInsetsCompat.Type.navigationBars())
                .bottom
            view.updatePadding(bottom = initialBottomPadding + navigationBarBottom)
            insets
        }
    }

    private fun applySystemBarAppearance() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = !isNightMode
        controller.isAppearanceLightNavigationBars = !isNightMode
    }

    private fun observeThemeMode() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userSettingsRepository.settings
                    .map { it.themeMode }
                    .distinctUntilChanged()
                    .collect { themeMode ->
                        val nightMode = themeMode.toNightMode()
                        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
                            applyThemeMode(themeMode)
                        }
                    }
            }
        }
    }

    private fun applyThemeMode(themeMode: ThemeMode) {
        AppCompatDelegate.setDefaultNightMode(themeMode.toNightMode())
    }

    private companion object {
        const val KEY_CURRENT_DESTINATION_ID = "current_destination_id"
    }
}

private fun ThemeMode.toNightMode(): Int =
    when (this) {
        ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    }
