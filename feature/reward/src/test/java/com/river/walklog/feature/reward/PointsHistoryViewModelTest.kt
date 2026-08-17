package com.river.walklog.feature.reward

import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetPointsHistoryUseCase
import com.river.walklog.core.model.MissionType
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakePointsLedgerRepository
import com.river.walklog.core.ui.UiText
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PointsHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var pointsLedgerRepository: FakePointsLedgerRepository
    private val crashReporter: CrashReporter = mockk(relaxed = true)

    private fun createViewModel() =
        PointsHistoryViewModel(GetPointsHistoryUseCase(pointsLedgerRepository), crashReporter)

    @Test
    fun `init sets SCREEN crash key to POINTS_HISTORY`() {
        pointsLedgerRepository = FakePointsLedgerRepository()

        createViewModel()

        verify { crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.POINTS_HISTORY) }
    }

    @Test
    fun `initial state has no groups when no ledger records`() {
        pointsLedgerRepository = FakePointsLedgerRepository()

        val viewModel = createViewModel()

        assertTrue(viewModel.state.value.groupedEntries.isEmpty())
        assertEquals(0, viewModel.state.value.totalNet)
    }

    @Test
    fun `maps ledger entries preserving delta and timestamp`() = runTest {
        pointsLedgerRepository = FakePointsLedgerRepository()
        pointsLedgerRepository.record(20, MissionType.DAILY.name)

        val viewModel = createViewModel()

        val entry = viewModel.state.value.groupedEntries.single().entries.single()
        assertEquals(20, entry.deltaPoints)
    }

    @Test
    fun `unknown reason maps to unknown reason text`() = runTest {
        pointsLedgerRepository = FakePointsLedgerRepository()
        pointsLedgerRepository.record(10, "SOME_UNRECOGNIZED_REASON")

        val viewModel = createViewModel()

        val entry = viewModel.state.value.groupedEntries.single().entries.single()
        assertEquals(R.string.points_history_reason_unknown, (entry.reasonText as UiText.StringRes).id)
    }

    @Test
    fun `totalNet sums all entries including negative redemptions`() = runTest {
        pointsLedgerRepository = FakePointsLedgerRepository()
        pointsLedgerRepository.record(20, MissionType.DAILY.name)
        pointsLedgerRepository.record(20, MissionType.RECOVERY.name)
        pointsLedgerRepository.record(-15, "REDEEM_something")

        val viewModel = createViewModel()

        assertEquals(25, viewModel.state.value.totalNet)
    }

    @Test
    fun `entries on the same day are grouped into a single group`() = runTest {
        pointsLedgerRepository = FakePointsLedgerRepository()
        pointsLedgerRepository.record(20, MissionType.DAILY.name)
        pointsLedgerRepository.record(20, MissionType.RECOVERY.name)

        val viewModel = createViewModel()

        assertEquals(1, viewModel.state.value.groupedEntries.size)
        assertEquals(2, viewModel.state.value.groupedEntries.single().entries.size)
    }

    @Test
    fun `today's group uses the today string resource label`() = runTest {
        pointsLedgerRepository = FakePointsLedgerRepository()
        pointsLedgerRepository.record(20, MissionType.DAILY.name)

        val viewModel = createViewModel()

        val label = viewModel.state.value.groupedEntries.single().dateLabel
        assertEquals(UiText.StringRes(R.string.points_history_today), label)
    }
}
