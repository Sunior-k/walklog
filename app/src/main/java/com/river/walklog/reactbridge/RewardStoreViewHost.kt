package com.river.walklog.reactbridge

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.callstack.reactnativebrownfield.ReactNativeBrownfield
import dagger.hilt.android.EntryPointAccessors

/**
 * [com.callstack.reactnativebrownfield.ReactNativeFragment]는 화면에 진입할 때마다
 * `ReactNativeBrownfield.createView()`를 새로 호출하는데, 그 내부에서 시스템 back 콜백
 * (`OnBackPressedCallback`)을 매번 새로 등록하고 절대 remove하지 않는다. 화면을 나갔다가
 * 다시 들어올 때마다 죽은 콜백이 디스패처에 계속 쌓이고, 새로 등록된 콜백이 back을
 * "RN에 처리할 스택 없음" 판단 후 다음 콜백으로 넘길 때 그 죽은 콜백이 먼저 걸려
 * 아무 반응도 하지 않는 상태가 된다 — 즉 두 번째 진입부터 시스템 back이 먹히지 않음.
 *
 * RN 루트 View(및 그 안의 ReactDelegateWrapper/OnBackPressedCallback)를 앱 생애주기 동안
 * 단 하나만 만들어 재사용해서 이 누수를 원천 차단한다. [RewardStoreFragment]는 이 View를
 * 매번 새로 만들지 않고 컨테이너에 붙였다 뗐다만 한다.
 */
object RewardStoreViewHost {
    private const val COMPONENT_NAME = "RewardStoreApp"

    private var cachedView: View? = null

    fun attachTo(activity: FragmentActivity, container: ViewGroup) {
        val view = cachedView ?: ReactNativeBrownfield.shared
            .createView(activity, COMPONENT_NAME)
            .also { cachedView = it }

        val currentParent = view.parent as? ViewGroup
        if (currentParent != null && currentParent !== container) {
            currentParent.removeView(view)
        }
        if (view.parent == null) {
            container.addView(view)
        }

        notifyScreenFocused(activity)
    }

    fun detachFrom(container: ViewGroup) {
        val view = cachedView ?: return
        if (view.parent === container) {
            container.removeView(view)
        }
    }

    /** RN 쪽 컴포넌트는 View 재사용으로 다시 마운트되지 않으므로, 화면 재진입 시 잔액/쿠폰/로그인 상태를 다시 조회하도록 알림. */
    private fun notifyScreenFocused(activity: FragmentActivity) {
        runCatching {
            ReactNativeBrownfield.shared.postMessage("""{"type":"SCREEN_FOCUSED"}""")
        }.onFailure { e ->
            val crashReporter = EntryPointAccessors.fromApplication(
                activity.applicationContext,
                RewardBridgeEntryPoint::class.java,
            ).crashReporter()
            crashReporter.log("RewardStoreViewHost: postMessage failed")
            crashReporter.recordException(e)
        }
    }
}
