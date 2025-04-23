package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.now
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.json.SimpleStringTypeAdapter
import com.google.gson.annotations.Expose
import kotlin.time.Duration

data class TimePeriod(
    @Expose val start: SimpleTimeMark,
    @Expose val end: SimpleTimeMark,
) {
    init {
        require(duration.isPositive()) { "TimePeriod cannot be empty" }
        require(start.toMillis() >= 0) { "start cannot be negative" }
        require(end.toMillis() >= 0) { "end cannot be negative" }
    }

    operator fun contains(time: SimpleTimeMark): Boolean = time in start..end
    operator fun contains(period: TimePeriod): Boolean = period.start >= start && period.end <= end
    operator fun plus(duration: Duration): TimePeriod = TimePeriod(start, end + duration)
    operator fun minus(duration: Duration): TimePeriod = TimePeriod(start, end - duration)
    operator fun plus(period: TimePeriod): TimePeriod = TimePeriod(min(start, period.start), max(end, period.end))

    override fun toString(): String = "${start.toMillis()}-${end.toMillis()}"

    fun formatString(): String = buildString {
        if (start.isInPast()) append("${start.passedSince()} ago")
        else append("in ${start.timeUntil()}")
        append(" --- ")
        if (end.isInPast()) append("${end.passedSince()} ago")
        else append("in ${end.timeUntil()}")
    }

    val duration: Duration get() = end - start

    fun timeUntilStart(): Duration = start - now()
    fun timeSinceEnd(): Duration = now() - end

    fun timeSinceStart(): Duration = now() - start
    fun timeUntilEnd(): Duration = end - now()

    fun isNow(): Boolean = now() in this
    fun isInPast(): Boolean = end < now()
    fun isInFuture(): Boolean = start > now()

    fun currentOrFuture(): Boolean = !isInPast()

    /** Assumes that the timePeriod isn't in the past. */
    fun getNextUpdate(): SimpleTimeMark = if (isNow()) end else start

    constructor(start: SkyBlockTime, end: SkyBlockTime) : this(start.toTimeMark(), end.toTimeMark())

    companion object {
        fun fromString(string: String): TimePeriod {
            val (first, second) = string.split("-").map { it.toLong().asTimeMark() }
            return TimePeriod(first, second)
        }
        infix fun SimpleTimeMark.until(other: SimpleTimeMark): TimePeriod = TimePeriod(this, other)

        val TYPE_ADAPTER = SimpleStringTypeAdapter(
            TimePeriod::toString,
            ::fromString,
        )

        fun Collection<TimePeriod>.getCurrentOrNext(): TimePeriod? {
            return this.filter(TimePeriod::currentOrFuture).minByOrNull(TimePeriod::getNextUpdate)
        }
    }
}
