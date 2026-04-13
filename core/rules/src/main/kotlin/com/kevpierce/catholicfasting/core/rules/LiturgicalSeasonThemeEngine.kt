package com.kevpierce.catholicfasting.core.rules

import com.kevpierce.catholicfasting.core.model.LiturgicalSeason
import java.time.LocalDate

object LiturgicalSeasonThemeEngine {
    fun seasonFor(date: LocalDate): LiturgicalSeason {
        val easter = DateSupport.easterSunday(date.year)
        val ashWednesday = easter.minusDays(46)
        val holySaturday = easter.minusDays(1)
        val pentecost = easter.plusDays(49)
        val adventStart = DateSupport.firstSundayOfAdvent(date.year)
        val christmasEve = LocalDate.of(date.year, 12, 24)

        return when {
            !date.isBefore(ashWednesday) && !date.isAfter(holySaturday) ->
                LiturgicalSeason.LENT
            !date.isBefore(easter) && !date.isAfter(pentecost) ->
                LiturgicalSeason.EASTER
            isInChristmasSeason(date) ->
                LiturgicalSeason.CHRISTMAS
            !date.isBefore(adventStart) && !date.isAfter(christmasEve) ->
                LiturgicalSeason.ADVENT
            else -> LiturgicalSeason.ORDINARY
        }
    }

    private fun isInChristmasSeason(date: LocalDate): Boolean {
        return when (date.monthValue) {
            12 -> !date.isBefore(LocalDate.of(date.year, 12, 25))
            1 -> !date.isAfter(LocalDate.of(date.year, 1, 13))
            else -> false
        }
    }
}
