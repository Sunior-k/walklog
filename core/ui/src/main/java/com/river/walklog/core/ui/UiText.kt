package com.river.walklog.core.ui

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes as StringResAnnotation

sealed interface UiText {

    data class DynamicString(val value: String) : UiText

    data class StringRes(
        @StringResAnnotation val id: Int,
        val args: List<Any> = emptyList(),
    ) : UiText

    data class PluralStringRes(
        @PluralsRes val id: Int,
        val count: Int,
        val args: List<Any> = emptyList(),
    ) : UiText
}

/** Context 기반으로 UiText를 문자열로 변환 */
fun UiText.asString(context: Context): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringRes -> if (args.isEmpty()) context.getString(id) else context.getString(id, *args.toTypedArray())
    is UiText.PluralStringRes -> if (args.isEmpty()) {
        context.resources.getQuantityString(id, count)
    } else {
        context.resources.getQuantityString(id, count, *args.toTypedArray())
    }
}

/** Composable 환경에서 UiText를 문자열로 변환 */
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringRes -> if (args.isEmpty()) {
        stringResource(id)
    } else {
        stringResource(id, *args.toTypedArray())
    }
    is UiText.PluralStringRes -> if (args.isEmpty()) {
        pluralStringResource(id, count)
    } else {
        pluralStringResource(id, count, *args.toTypedArray())
    }
}
