package com.github.itsempa.nautilus.data.fishingevents

import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.utils.SkyBlockTime
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusUtils.skyblockDays
import com.github.itsempa.nautilus.utils.NautilusUtils.skyblockYears
import com.github.itsempa.nautilus.utils.TimePeriod
import com.github.itsempa.nautilus.utils.TimePeriod.Companion.getCurrentOrNext
import kotlin.time.Duration

@Module
data object SpookyFestivalEvent : FishingEvent("SPOOKY_FESTIVAL") {
    override val name: String = "Spooky Festival"
    override val duration: Duration = 9.skyblockDays

    override fun updateNextTimePeriod(): TimePeriod? {
        val currentYear = SkyBlockTime.now().year
        val list = buildList {
            fun add(time: SkyBlockTime, duration: Duration = this@SpookyFestivalEvent.duration) =
                add(TimePeriod(time, time + duration))
            val first = SkyBlockTime(currentYear, 8, 26)
            add(first)
            add(first + 1.skyblockYears)
            if (Perk.EXTRA_EVENT_SPOOKY.isActive) {
                // TODO: add foxy extra spooky event
            }
            // TODO: add halloween extra events
        }
        return list.getCurrentOrNext()
    }
}
