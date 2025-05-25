package com.github.itsempa.nautilus.data.fishingevents

import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.TimeUtils
import com.github.itsempa.nautilus.utils.NautilusUtils.skyblockDays
import com.github.itsempa.nautilus.utils.NautilusUtils.skyblockYears
import com.github.itsempa.nautilus.utils.TimePeriod
import com.github.itsempa.nautilus.utils.TimePeriod.Companion.getCurrentOrNext
import com.github.itsempa.nautilus.utils.TimePeriod.Companion.until
import me.owdding.ktmodules.Module
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZoneOffset
import kotlin.time.Duration

@Module
data object JerrysWorkshopEvent : FishingEvent("JERRY") {
    override val name: String = "Jerry's Workshop"
    override val duration: Duration = 31.skyblockDays

    override fun updateNextTimePeriod(): TimePeriod? {
        val currentYear = SkyBlockTime.now().year
        val list = buildList {
            fun add(time: SkyBlockTime, duration: Duration = this@JerrysWorkshopEvent.duration) =
                add(TimePeriod(time, time + duration))

            val first = SkyBlockTime(currentYear, 12, 1)
            add(first)
            add(first + 1.skyblockYears)
            val irlYear = TimeUtils.getCurrentLocalDate().year
            add(getDecemberTimePeriod(irlYear))
        }
        return list.getCurrentOrNext()
    }

    private fun getDecemberTimePeriod(year: Int): TimePeriod {
        val start = LocalDate.of(year, Month.DECEMBER, 1).atStartOfDay(ZoneOffset.UTC).toInstant()
        val end = LocalDate.of(year, Month.DECEMBER, 31).atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).toInstant()
        return start.toEpochMilli().asTimeMark() until end.toEpochMilli().asTimeMark()
    }
}
