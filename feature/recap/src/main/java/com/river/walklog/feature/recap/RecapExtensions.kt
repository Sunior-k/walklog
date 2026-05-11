package com.river.walklog.feature.recap

import androidx.annotation.StringRes
import com.river.walklog.core.model.MonthlyRecap

@StringRes
fun MonthlyRecap.walkerPersonaRes(): Int = when {
    averageStepsPerDay >= 10_000 -> R.string.persona_perfect_walker
    averageStepsPerDay >= 7_000 -> R.string.persona_steady_achiever
    averageStepsPerDay >= 5_000 -> R.string.persona_diligent_challenger
    averageStepsPerDay >= 3_000 -> R.string.persona_promising_beginner
    else -> R.string.persona_newcomer
}

@StringRes
fun MonthlyRecap.walkerPersonaDescRes(): Int = when {
    averageStepsPerDay >= 10_000 -> R.string.persona_desc_perfect_walker
    averageStepsPerDay >= 7_000 -> R.string.persona_desc_steady_achiever
    averageStepsPerDay >= 5_000 -> R.string.persona_desc_diligent_challenger
    averageStepsPerDay >= 3_000 -> R.string.persona_desc_promising_beginner
    else -> R.string.persona_desc_newcomer
}
