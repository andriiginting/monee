package data

private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

private val MONTH_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

private val DAY_NAMES = listOf(
    "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
)

/** The `Oct 24, 2026` form receipt drafts store their date in. */
private val storedDate = Regex("""^([A-Za-z]{3})\s+(\d{1,2}),\s+(\d{4})$""")

/** A calendar day, independent of time zone. */
data class CalendarDay(val year: Int, val month: Int, val day: Int) : Comparable<CalendarDay> {
    val epochDay: Long get() = daysFromCivil(year, month, day)

    /** `OCTOBER 24, 2026 (THURSDAY)`, the Activity section heading. */
    val heading: String
        get() {
            val weekday = DAY_NAMES[(epochDay + 3).mod(7L).toInt()]
            return "${MONTH_NAMES[month - 1]} $day, $year ($weekday)".uppercase()
        }

    override fun compareTo(other: CalendarDay): Int = epochDay.compareTo(other.epochDay)

    companion object {
        fun fromEpochMillis(millis: Long): CalendarDay = civilFromDays(millis.floorDiv(MILLIS_PER_DAY))

        fun today(): CalendarDay = fromEpochMillis(kotlin.time.Clock.System.now().toEpochMilliseconds())
    }
}

/**
 * The day an expense belongs to. The receipt's printed date wins, because an
 * expense scanned the next morning still happened the day before; the posting
 * time is the fallback for receipts that printed no readable date. Posting time
 * is read in UTC since the project has no time zone library yet.
 */
val Expense.calendarDay: CalendarDay
    get() = date?.let(::parseStoredDate) ?: CalendarDay.fromEpochMillis(createdAtMillis)

private fun parseStoredDate(raw: String): CalendarDay? {
    val (monthName, day, year) = storedDate.find(raw.trim())?.destructured ?: return null
    val month = MONTH_NAMES.indexOfFirst { it.startsWith(monthName, ignoreCase = true) } + 1
    if (month == 0) return null
    return CalendarDay(year.toInt(), month, day.toInt())
}

// Howard Hinnant's civil-calendar algorithms (proleptic Gregorian).
private fun daysFromCivil(year: Int, month: Int, day: Int): Long {
    val y = (if (month <= 2) year - 1 else year).toLong()
    val era = y.floorDiv(400L)
    val yearOfEra = y - era * 400
    val monthIndex = (month + 9) % 12
    val dayOfYear = (153 * monthIndex + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146097 + dayOfEra - 719468
}

private fun civilFromDays(epochDay: Long): CalendarDay {
    val z = epochDay + 719468
    val era = z.floorDiv(146097L)
    val dayOfEra = z - era * 146097
    val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthIndex = (5 * dayOfYear + 2) / 153
    val day = (dayOfYear - (153 * monthIndex + 2) / 5 + 1).toInt()
    val month = (if (monthIndex < 10) monthIndex + 3 else monthIndex - 9).toInt()
    val year = (yearOfEra + era * 400 + if (month <= 2) 1 else 0).toInt()
    return CalendarDay(year, month, day)
}
