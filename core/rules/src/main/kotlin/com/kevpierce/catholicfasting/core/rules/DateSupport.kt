package com.kevpierce.catholicfasting.core.rules

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

object DateSupport {
    val dayKeyFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun parseDayKey(value: String): LocalDate = LocalDate.parse(value, dayKeyFormatter)

    fun formatDayKey(date: LocalDate): String = date.format(dayKeyFormatter)

    fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }

    fun firstSundayOfAdvent(year: Int): LocalDate {
        var cursor = LocalDate.of(year, Month.NOVEMBER, 27)
        while (cursor.dayOfWeek != DayOfWeek.SUNDAY) {
            cursor = cursor.plusDays(1)
        }
        return cursor
    }

    fun epiphanySunday(year: Int): LocalDate {
        return (2..8)
            .asSequence()
            .map { LocalDate.of(year, Month.JANUARY, it) }
            .firstOrNull { it.dayOfWeek == DayOfWeek.SUNDAY }
            ?: LocalDate.of(year, Month.JANUARY, 6)
    }

    fun holyFamilyDate(year: Int): LocalDate {
        return (26..31)
            .asSequence()
            .map { LocalDate.of(year, Month.DECEMBER, it) }
            .firstOrNull { it.dayOfWeek == DayOfWeek.SUNDAY }
            ?: LocalDate.of(year, Month.DECEMBER, 30)
    }

    fun nextWeekdayOnOrAfter(
        date: LocalDate,
        dayOfWeek: DayOfWeek,
    ): LocalDate {
        var cursor = date
        while (cursor.dayOfWeek != dayOfWeek) {
            cursor = cursor.plusDays(1)
        }
        return cursor
    }

    fun nextWeekdayAfter(
        date: LocalDate,
        dayOfWeek: DayOfWeek,
    ): LocalDate {
        return nextWeekdayOnOrAfter(date.plusDays(1), dayOfWeek)
    }

    fun ageOn(
        date: LocalDate,
        birthYear: Int,
        birthMonth: Int,
        birthDay: Int,
    ): Int {
        val baseAge = date.year - birthYear
        val anniversary =
            if (birthMonth in 1..12 && birthDay in 1..31) {
                runCatching { LocalDate.of(date.year, birthMonth, birthDay) }.getOrNull()
            } else {
                null
            }

        return when {
            birthYear < 1900 -> 0
            anniversary == null -> maxOf(0, baseAge)
            date.isBefore(anniversary) -> baseAge - 1
            else -> baseAge
        }
    }
}
