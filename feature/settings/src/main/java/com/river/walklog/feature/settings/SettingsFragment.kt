package com.river.walklog.feature.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.river.walklog.core.auth.GoogleSignInResult
import com.river.walklog.core.auth.getGoogleIdToken
import com.river.walklog.core.designsystem.foundation.PremiumFlatPalette
import com.river.walklog.core.designsystem.foundation.setPremiumCardColor
import com.river.walklog.core.model.PremiumVisualMode
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.ui.ThemeViewModel
import com.river.walklog.feature.settings.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import com.river.walklog.core.designsystem.R as DesignR

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

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
        observePremiumTheme()
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
        switchPremiumTheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.togglePremiumTheme(isChecked)
        }
        premiumThemeCustomizeRow.setOnClickListener { navigateToPremiumThemeSettings() }
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

        if (switchPremiumTheme.isChecked != state.isPremiumThemeActive) {
            switchPremiumTheme.isChecked = state.isPremiumThemeActive
        }
        tvPremiumThemeChip.isVisible = state.isPremiumThemeOwned
        premiumThemeCustomizeRow.isVisible = state.isPremiumThemeOwned
        tvPremiumThemeDescription.text = getString(
            if (state.isPremiumThemeOwned) {
                R.string.settings_premium_theme_description_owned
            } else {
                R.string.settings_premium_theme_description_locked
            },
        )

        val isEnabled = !state.isLoading
        seekDailyStepGoal.isEnabled = isEnabled
        seekRecoveryMissionSteps.isEnabled = isEnabled
        switchNotifications.isEnabled = isEnabled
        radioThemeSystem.isEnabled = isEnabled
        radioThemeLight.isEnabled = isEnabled
        radioThemeDark.isEnabled = isEnabled
        switchPremiumTheme.isEnabled = isEnabled && state.isPremiumThemeOwned
    }

    private fun updateThemeMode(themeMode: ThemeMode) {
        viewModel.updateThemeMode(themeMode)
    }

    private fun observePremiumTheme() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(themeViewModel.isPremiumTheme, themeViewModel.premiumVisualMode) { active, mode ->
                    if (active) mode else null
                }.collectLatest { mode -> applyPremiumTheme(mode) }
            }
        }
    }

    private fun applyPremiumTheme(mode: PremiumVisualMode?) = with(binding) {
        val ctx = requireContext()
        val defaultBackground = ContextCompat.getColor(ctx, DesignR.color.walklog_background)
        val defaultCard = ContextCompat.getColor(ctx, DesignR.color.walklog_gray_50)
        val defaultOnBackground = ContextCompat.getColor(ctx, DesignR.color.walklog_text_primary)
        val defaultOnBackgroundMuted = ContextCompat.getColor(ctx, DesignR.color.walklog_text_secondary)
        val defaultOnCard = ContextCompat.getColor(ctx, DesignR.color.walklog_text_primary)
        val defaultOnCardMuted = ContextCompat.getColor(ctx, DesignR.color.walklog_text_secondary)
        val palette = mode?.let { PremiumFlatPalette.forMode(it) }
        val isNight = mode == PremiumVisualMode.NIGHT
        val cardColor = palette?.cardBackground ?: defaultCard

        root.setBackgroundColor(palette?.background ?: defaultBackground)
        listOf(
            profileCard,
            googleAccountCard,
            dailyStepGoalCard,
            recoveryMissionCard,
            notificationsCard,
            themeCard,
            premiumThemeCard,
            premiumThemeCustomizeRow,
            layoutOssLicenses,
        ).forEach { it.setPremiumCardColor(cardColor) }

        // 화면 제목·버전 정보는 카드로 감싸이지 않고 배경 위에 바로 놓여 있다.
        val onBackground = palette?.onBackground ?: defaultOnBackground
        val onBackgroundMuted = palette?.onBackgroundMuted ?: defaultOnBackgroundMuted
        tvTitle.setTextColor(onBackground)
        tvAppVersion.setTextColor(onBackgroundMuted)

        // 카드 위 주요 텍스트 — NIGHT는 카드 자체가 어두워지므로 밝은 색으로 뒤집혀야 한다.
        val onCard = palette?.onCard ?: defaultOnCard
        val onCardMuted = palette?.onCardMuted ?: defaultOnCardMuted
        tvNickname.setTextColor(onCard)
        tvGoogleEmail.setTextColor(onCard)
        tvDailyStepGoalTitle.setTextColor(onCard)
        tvRecoveryMissionTitle.setTextColor(onCard)
        tvNotificationsTitle.setTextColor(onCard)
        tvThemeTitle.setTextColor(onCard)
        listOf(radioThemeSystem, radioThemeLight, radioThemeDark).forEach { it.setTextColor(onCard) }
        tvPremiumThemeTitle.setTextColor(onCard)
        tvPremiumThemeCustomizeTitle.setTextColor(onCard)
        ivPremiumThemeCustomizeArrow.setColorFilter(onCardMuted)
        tvOssLicensesTitle.setTextColor(onCard)
        ivOssLicensesArrow.setColorFilter(onCardMuted)
        // 회복 미션 걸음 수(블루 강조)는 NIGHT의 남색 카드 위에서 대비가 약해지므로 밝은 색으로 대체한다.
        tvRecoveryMissionSteps.setTextColor(
            if (isNight) onCard else ContextCompat.getColor(ctx, DesignR.color.walklog_secondary),
        )

        // 아바타·포인트 배지는 원래 옅은 골드 배경 + 진한 텍스트라, NIGHT의 어두운 카드 위에서는
        // 그대로 두면 밝은 상자가 튀어 보인다. NIGHT에서는 반투명 골드 배경으로 낮춘다.
        val defaultBadgeBg = ContextCompat.getColor(ctx, DesignR.color.walklog_primary_container)
        val badgeBg = if (isNight) withAlpha(ContextCompat.getColor(ctx, DesignR.color.walklog_primary), 56) else defaultBadgeBg
        tvAvatar.setPremiumCardColor(badgeBg)
        tvPointsBadge.setPremiumCardColor(badgeBg)
    }

    private fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha shl 24)

    private fun navigateToPremiumThemeSettings() {
        val actionId = resources.getIdentifier(
            "action_settings_to_premiumThemeSettings",
            "id",
            requireContext().packageName,
        )
        if (actionId != 0) findNavController().navigate(actionId)
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
