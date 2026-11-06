package com.kookyar.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kookyar.app.AppGraph
import com.kookyar.app.core.i18n.AppLanguage
import com.kookyar.app.core.i18n.stringsOf
import com.kookyar.app.data.model.TuningRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HistoryViewModel : ViewModel() {

    data class DayStat(
        val label: String,
        val accuracy: Int,
        val count: Int,
        val isToday: Boolean
    )

    data class HistoryUiState(
        val records: List<TuningRecord> = emptyList(),
        val week: List<DayStat> = emptyList(),
        val weekCount: Int = 0,
        val avgAccuracy: Int = 0,
        val bestDay: String = "—",
        val bestDayAccuracy: Int = 0,
        val trendUp: Boolean = true,
        val trendDelta: Int = 0
    )

    private val _ui = MutableStateFlow(HistoryUiState())
    val ui: StateFlow<HistoryUiState> = _ui.asStateFlow()

    @Volatile
    private var lang: AppLanguage = AppLanguage.FA

    private var lastRecords: List<TuningRecord> = emptyList()

    init {
        viewModelScope.launch {
            AppGraph.settingsStore.settings.collect { s ->
                if (s.language != lang) {
                    lang = s.language
                    _ui.value = compute(lastRecords)
                }
            }
        }
        viewModelScope.launch {
            AppGraph.historyStore.records.collect { records ->
                lastRecords = records
                _ui.value = compute(records)
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch { AppGraph.historyStore.clear() }
    }

    private fun compute(records: List<TuningRecord>): HistoryUiState {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        val byDate = records.groupBy { rec ->
            Instant.ofEpochMilli(rec.at).atZone(zone).toLocalDate()
        }

        val faWeekday: Map<DayOfWeek, String> = DayOfWeek.values().associateWith {
            stringsOf(lang).weekday(it)
        }

        val week = (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val dayRecords = byDate[date].orEmpty()
            DayStat(
                label = faWeekday[date.dayOfWeek] ?: "",
                accuracy = if (dayRecords.isEmpty()) 0 else dayRecords.map { it.accuracy }.average().toInt(),
                count = dayRecords.size,
                isToday = offset == 0
            )
        }

        val thisWeekRecords = (0..6).flatMap { offset ->
            byDate[today.minusDays(offset.toLong())].orEmpty()
        }
        val lastWeekRecords = (7..13).flatMap { offset ->
            byDate[today.minusDays(offset.toLong())].orEmpty()
        }

        val avgThis = if (thisWeekRecords.isEmpty()) 0 else thisWeekRecords.map { it.accuracy }.average().toInt()
        val avgLast = if (lastWeekRecords.isEmpty()) avgThis else lastWeekRecords.map { it.accuracy }.average().toInt()

        val best = week.filter { it.count > 0 }.maxByOrNull { it.accuracy }

        val bestDayLabel = best?.label ?: (if (lang.isFa) "—" else "–")

        return HistoryUiState(
            records = records.take(60),
            week = week,
            weekCount = thisWeekRecords.size,
            avgAccuracy = avgThis,
            bestDay = bestDayLabel,
            bestDayAccuracy = best?.accuracy ?: 0,
            trendUp = avgThis >= avgLast,
            trendDelta = avgThis - avgLast
        )
    }
}
