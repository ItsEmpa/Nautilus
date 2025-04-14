package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.now
import at.hannibal2.skyhanni.utils.SkyBlockTime
import com.google.gson.annotations.Expose
import kotlin.time.Duration

data class TimePeriod(
    @Expose val start: SimpleTimeMark,
    @Expose val end: SimpleTimeMark,
) {
    init {
        require(duration.isPositive()) { "TimePeriod cannot be empty" }
    }

    operator fun contains(time: SimpleTimeMark): Boolean = time in start..end
    operator fun contains(period: TimePeriod): Boolean = period.start >= start && period.end <= end
    operator fun plus(duration: Duration): TimePeriod = TimePeriod(start, end + duration)
    operator fun minus(duration: Duration): TimePeriod = TimePeriod(start, end - duration)
    operator fun plus(period: TimePeriod): TimePeriod = TimePeriod(min(start, period.start), max(end, period.end))

    val duration: Duration get() = end - start

    fun timeUntilStart(): Duration = start - now()
    fun timeSinceEnd(): Duration = now() - end

    fun timeSinceStart(): Duration = now() - start
    fun timeUntilEnd(): Duration = end - now()

    fun isNow(): Boolean = now() in this
    fun isInPast(): Boolean = start > now()
    fun isInFuture(): Boolean = end < now()

    constructor(start: SimpleTimeMark, duration: Duration) : this(start, start + duration)
    constructor(start: SkyBlockTime, end: SkyBlockTime) : this(start.asTimeMark(), end.asTimeMark())

    companion object {
        infix fun SimpleTimeMark.until(other: SimpleTimeMark): TimePeriod = TimePeriod(this, other)
        infix fun SimpleTimeMark.until(duration: Duration): TimePeriod = TimePeriod(this, duration)
    }
}
