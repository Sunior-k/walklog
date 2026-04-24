package com.river.walklog

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.datastore.readStoredThemeMode
import com.river.walklog.core.model.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    private val bottomNavDestinations = setOf(
        R.id.homeFragment,
        R.id.historyFragment,
        R.id.settingsFragment,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedThemeBeforeCreate()
        super.onCreate(savedInstanceState)

        val startDestination = if (savedInstanceState == null) {
            runBlocking {
                if (userSettingsRepository.settings.first().isOnboardingCompleted) {
                    R.id.homeFragment
                } else {
                    R.id.onboardingFragment
                }
            }
        } else {
            null
        }
        val restoredDestination = savedInstanceState
            ?.getInt(KEY_CURRENT_DESTINATION_ID)
            ?.takeIf { it != 0 }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        applySystemBarAppearance()

        val navController = findNavController()
        if (startDestination != null) {
            setStartDestination(navController, startDestination)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bindBottomNavigation(bottomNav, navController)
        applyBottomNavigationInsets(bottomNav)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.isVisible = destination.id in bottomNavDestinations
            if (destination.id in bottomNavDestinations) {
                bottomNav.menu.findItem(destination.id)?.isChecked = true
            }
        }

        bottomNav.post {
            if (navController.currentDestination == null) {
                val fallbackDestination = restoredDestination ?: startDestination ?: R.id.homeFragment
                setStartDestination(navController, fallbackDestination)
            }
        }

        observeThemeMode()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        findNavControllerOrNull()
            ?.currentDestination
            ?.id
            ?.also { destinationId -> outState.putInt(KEY_CURRENT_DESTINATION_ID, destinationId) }
        super.onSaveInstanceState(outState)
    }

    private fun applySavedThemeBeforeCreate() {
        val savedTheme = runBlocking { applicationContext.readStoredThemeMode() }
        AppCompatDelegate.setDefaultNightMode(savedTheme.toNightMode())
    }

    private fun findNavController(): NavController {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHost.navController
    }

    private fun findNavControllerOrNull(): NavController? {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        return navHost?.navController
    }

    private fun setStartDestination(
        navController: NavController,
        destinationId: Int,
    ) {
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(destinationId)
        navController.setGraph(graph, null)
    }

    private fun bindBottomNavigation(
        bottomNav: BottomNavigationView,
        navController: NavController,
    ) {
        bottomNav.setOnItemReselectedListener { }
        bottomNav.setOnItemSelectedListener { item ->
            val currentDestination = navController.currentDestination
                ?: return@setOnItemSelectedListener false
            if (currentDestination.id == item.itemId) {
                return@setOnItemSelectedListener true
            }

            val options = navOptions {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
            }

            runCatching {
                navController.navigate(item.itemId, null, options)
            }.isSuccess
        }
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
                            AppCompatDelegate.setDefaultNightMode(nightMode)
                        }
                    }
            }
        }
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
