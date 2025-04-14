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

    inline val asString: String get() = toString()

    override fun toString(): String = "${start.toMillis()}-${end.toMillis()}"

    val duration: Duration get() = end - start

    fun timeUntilStart(): Duration = start - now()
    fun timeSinceEnd(): Duration = now() - end

    fun timeSinceStart(): Duration = now() - start
    fun timeUntilEnd(): Duration = end - now()

    fun isNow(): Boolean = now() in this
    fun isInPast(): Boolean = start > now()
    fun isInFuture(): Boolean = end < now()

    constructor(start: Long, end: Long) : this(start.asTimeMark(), end.asTimeMark())
    constructor(start: SimpleTimeMark, duration: Duration) : this(start, start + duration)
    constructor(start: SkyBlockTime, end: SkyBlockTime) : this(start.asTimeMark(), end.asTimeMark())

    companion object {
        fun fromString(string: String): TimePeriod {
            val (first, second) = string.split("-").map { it.toLong() }
            return TimePeriod(first, second)
        }
        infix fun SimpleTimeMark.until(other: SimpleTimeMark): TimePeriod = TimePeriod(this, other)
        infix fun SimpleTimeMark.until(duration: Duration): TimePeriod = TimePeriod(this, duration)

        val TYPE_ADAPTER = SimpleStringTypeAdapter(
            TimePeriod::asString,
            ::fromString,
        )
    }
}
