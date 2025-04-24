package com.github.itsempa.nautilus.data.fishingevents

import at.hannibal2.skyhanni.utils.SkyBlockTime
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.TimePeriod
import com.github.itsempa.nautilus.utils.TimePeriod.Companion.getCurrentOrNext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@Module
data object JerrysWorkshopEvent : FishingEvent("JERRY") {
    override val name: String = "Jerry's Workshop"
    override val duration: Duration = 8.hours // TODO: confirm

    override fun updateNextTimePeriod(): TimePeriod? {
        val currentYear = SkyBlockTime.now().year
        val list = buildList {
            fun add(time: SkyBlockTime, duration: Duration = this@JerrysWorkshopEvent.duration) =
                add(TimePeriod(time, time + duration))

            add(SkyBlockTime(currentYear, 12, 1))
            add(SkyBlockTime(currentYear + 1, 12, 1))
            // TODO: implement continuous winter workshop on december
        }
        return list.getCurrentOrNext()
    }
}
