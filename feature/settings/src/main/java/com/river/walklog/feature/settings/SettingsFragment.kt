package com.river.walklog.feature.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.river.walklog.core.auth.GoogleSignInResult
import com.river.walklog.core.auth.getGoogleIdToken
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.feature.settings.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyStatusBarInsets()
        setupListeners()
        observeState()
    }

    private fun applyStatusBarInsets() {
        val initialTopPadding = binding.root.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarTop = insets
                .getInsets(WindowInsetsCompat.Type.statusBars())
                .top
            view.updatePadding(top = initialTopPadding + statusBarTop)
            insets
        }
    }

    private fun setupListeners() = with(binding) {
        profileCard.setOnClickListener { showNicknameEditDialog() }
        btnGoogleSignIn.setOnClickListener { launchGoogleSignIn() }
        btnSignOut.setOnClickListener { viewModel.signOut() }
        seekDailyStepGoal.setOnSeekBarChangeListener(
            onProgressChanged = { progress, fromUser ->
                if (fromUser) viewModel.updateStepGoal(progress.toSteps(min = DAILY_STEP_MIN))
            },
        )
        seekRecoveryMissionSteps.setOnSeekBarChangeListener(
            onProgressChanged = { progress, fromUser ->
                if (fromUser) viewModel.updateRecoverySteps(progress.toSteps(min = RECOVERY_STEP_MIN))
            },
        )
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateNotifications(isChecked)
        }
        radioThemeSystem.setOnClickListener { updateThemeMode(ThemeMode.SYSTEM) }
        radioThemeLight.setOnClickListener { updateThemeMode(ThemeMode.LIGHT) }
        radioThemeDark.setOnClickListener { updateThemeMode(ThemeMode.DARK) }
        layoutOssLicenses.setOnClickListener {
            startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: SettingsState) = with(binding) {
        val displayName = state.nickname.ifBlank { getString(R.string.anonymous) }
        val numberFormat = NumberFormat.getNumberInstance(resources.configuration.locales[0])
        tvNickname.text = getString(R.string.settings_nickname_display, displayName)
        tvAvatar.text = displayName.take(1).uppercase()
        tvPointsBadge.text = numberFormat.format(state.totalPoints) + " P"

        // Google 계정 섹션
        if (state.isSignedIn) {
            tvGoogleEmail.isVisible = true
            btnSignOut.isVisible = true
            btnGoogleSignIn.isVisible = false
            tvGoogleEmail.text = state.userEmail
        } else {
            tvGoogleEmail.isVisible = false
            btnSignOut.isVisible = false
            btnGoogleSignIn.isVisible = true
        }

        tvDailyStepGoal.text = state.dailyStepGoal.toStepText(numberFormat)
        tvRecoveryMissionSteps.text = state.recoveryMissionSteps.toStepText(numberFormat)

        seekDailyStepGoal.progress = state.dailyStepGoal.toProgress(min = DAILY_STEP_MIN)
        seekRecoveryMissionSteps.progress = state.recoveryMissionSteps.toProgress(min = RECOVERY_STEP_MIN)

        if (switchNotifications.isChecked != state.notificationsEnabled) {
            switchNotifications.isChecked = state.notificationsEnabled
        }
        radioThemeSystem.isChecked = state.themeMode == ThemeMode.SYSTEM
        radioThemeLight.isChecked = state.themeMode == ThemeMode.LIGHT
        radioThemeDark.isChecked = state.themeMode == ThemeMode.DARK

        val isEnabled = !state.isLoading
        seekDailyStepGoal.isEnabled = isEnabled
        seekRecoveryMissionSteps.isEnabled = isEnabled
        switchNotifications.isEnabled = isEnabled
        radioThemeSystem.isEnabled = isEnabled
        radioThemeLight.isEnabled = isEnabled
        radioThemeDark.isEnabled = isEnabled
    }

    private fun updateThemeMode(themeMode: ThemeMode) {
        viewModel.updateThemeMode(themeMode)
    }

    private fun showNicknameEditDialog() {
        val sheet = NicknameEditBottomSheet.newInstance(viewModel.state.value.nickname)
        sheet.onSave = { newNickname -> viewModel.updateNickname(newNickname) }
        sheet.show(childFragmentManager, NicknameEditBottomSheet.TAG)
    }

    private fun launchGoogleSignIn() {
        val clientId = getString(R.string.default_web_client_id)
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = getGoogleIdToken(requireActivity(), clientId)) {
                is GoogleSignInResult.Success -> viewModel.onGoogleIdTokenReceived(result.idToken)
                is GoogleSignInResult.Cancelled -> Unit
                is GoogleSignInResult.Failed -> Unit
            }
        }
    }

    private fun Int.toStepText(numberFormat: NumberFormat): String =
        getString(R.string.settings_steps_format, numberFormat.format(this))

    private fun Int.toProgress(min: Int): Int = ((this - min) / STEP_INTERVAL).coerceAtLeast(0)

    private fun Int.toSteps(min: Int): Int = min + (this * STEP_INTERVAL)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val DAILY_STEP_MIN = 5_000
        const val RECOVERY_STEP_MIN = 3_000
        const val STEP_INTERVAL = 500
    }
}

private fun SeekBar.setOnSeekBarChangeListener(
    onProgressChanged: (progress: Int, fromUser: Boolean) -> Unit,
) {
    setOnSeekBarChangeListener(
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onProgressChanged(progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        },
    )
}
