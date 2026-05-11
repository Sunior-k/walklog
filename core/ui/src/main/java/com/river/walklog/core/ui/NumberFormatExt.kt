package com.river.walklog.core.ui

import java.text.NumberFormat
import java.util.Locale

/** Int에 천 단위 구분 쉼표 추가해서 문자열로 반환 */
fun Int.withComma(): String = NumberFormat.getNumberInstance(Locale.getDefault()).format(this)
