package com.lab03g10.smartpantry.ui.viewmodel


import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update


/**
 * @(#) ExpiryCalendarViewModel.kt 1.0 2026-05-18
 * Copyright (c) 2026 the SmartPantry team.
 *
 * Backs `ExpiryCalendarScreen`, replacing the A2-prototype hardcoded April
 * 2026 calendar + fake food events with a real `FoodRepository`-driven
 * month grid that the user can navigate prev/next. Fixes BUG-007 (see
 * docs/bug-log.md).
 */

// Same palette tokens as the screen, duplicated here so the VM doesn't have
// to import from a UI package. If the design system grows, move these to a
// shared `ui.theme.urgency` module.
private val ExpiredGrey = Color(0xFF9E9E9E)
private val UrgentRed   = Color(0xFFBA1A1A)
private val WarnOrange  = Color(0xFFF57F17)
private val SafeGreen   = Color(0xFF2E7D32)
private val TodayGreen  = Color(0xFF0F6E56)

data class CalendarDayCell(
    val dayOfMonth: Int,
    val date: LocalDate,
    /** Foods whose effectiveExpiryDate falls on this calendar day. */
    val events: List<FoodItemEntity>,
    /** Tint for the urgency dot; null when there are no events on this day. */
    val mostUrgentColor: Color?,
    val isToday: Boolean,
)

data class ExpiryCalendarUiState(
    val displayedMonth: YearMonth = YearMonth.now(),
    /** 7 cells per row, null cells = leading/trailing padding outside the month. */
    val weeks: List<List<CalendarDayCell?>> = emptyList(),
    val selectedDay: CalendarDayCell? = null,
)

@HiltViewModel
class ExpiryCalendarViewModel @Inject constructor(
    foodRepository: FoodRepository,
) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val month = MutableStateFlow(YearMonth.now())
    /** Day-of-month of the user's selected cell; persists across month nav so the
     *  cursor "follows" prev/next presses. Null until the user taps. */
    private val selectedDay = MutableStateFlow<Int?>(null)

    val state: StateFlow<ExpiryCalendarUiState> = combine(
        month,
        selectedDay,
        foodRepository.observeAll(),
    ) { ym, selDay, allFoods ->
        val today = LocalDate.now(zone)
        // Foods whose effective-expiry day falls in the displayed month,
        // grouped by day-of-month for O(1) cell lookups in buildMonthGrid.
        val foodsByDay: Map<Int, List<FoodItemEntity>> = allFoods
            .filter { food ->
                val d = food.effectiveExpiryDate.toLocalDate(zone)
                YearMonth.from(d) == ym
            }
            .groupBy { it.effectiveExpiryDate.toLocalDate(zone).dayOfMonth }

        ExpiryCalendarUiState(
            displayedMonth = ym,
            weeks          = buildMonthGrid(ym, foodsByDay, today),
            selectedDay    = selDay?.takeIf { it in 1..ym.lengthOfMonth() }?.let { day ->
                buildCell(ym, day, foodsByDay[day].orEmpty(), today)
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpiryCalendarUiState())

    fun onPrevMonth() = month.update { it.minusMonths(1) }
    fun onNextMonth() = month.update { it.plusMonths(1) }
    fun onDayTap(day: Int) = selectedDay.update { day }
}

// ---- helpers (file-level so they're testable without the VM in scope) -----

private fun Long.toLocalDate(zone: ZoneId): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

/** Most urgent event in the cell drives the dot colour. */
private fun buildCell(
    ym: YearMonth,
    day: Int,
    events: List<FoodItemEntity>,
    today: LocalDate,
): CalendarDayCell {
    val date = ym.atDay(day)
    val mostUrgentDeltaDays = events.minOfOrNull { food ->
        ChronoUnit.DAYS.between(today, food.effectiveExpiryDate.toLocalDate(ZoneId.systemDefault()))
    }
    return CalendarDayCell(
        dayOfMonth      = day,
        date            = date,
        events          = events,
        mostUrgentColor = mostUrgentDeltaDays?.let(::colorForDelta),
        isToday         = date == today,
    )
}

private fun colorForDelta(daysFromToday: Long): Color = when {
    daysFromToday < 0L  -> ExpiredGrey   // already past
    daysFromToday == 0L -> TodayGreen    // today
    daysFromToday <= 3L -> UrgentRed     // 1-3 days
    daysFromToday <= 7L -> WarnOrange    // 4-7 days
    else                -> SafeGreen     // 8+
}

/** Builds a Monday-first 7-wide grid for the month, padding leading + trailing
 *  cells with `null` so the screen can render a fixed-width row layout. */
private fun buildMonthGrid(
    ym: YearMonth,
    foodsByDay: Map<Int, List<FoodItemEntity>>,
    today: LocalDate,
): List<List<CalendarDayCell?>> {
    // DayOfWeek.MONDAY.value == 1 → 0 leading nulls when the 1st is a Monday.
    val leadingNulls = ym.atDay(1).dayOfWeek.value - 1
    val daysInMonth  = ym.lengthOfMonth()
    val cells = buildList<CalendarDayCell?> {
        repeat(leadingNulls) { add(null) }
        for (d in 1..daysInMonth) {
            add(buildCell(ym, d, foodsByDay[d].orEmpty(), today))
        }
        while (size % 7 != 0) add(null)
    }
    return cells.chunked(7)
}
