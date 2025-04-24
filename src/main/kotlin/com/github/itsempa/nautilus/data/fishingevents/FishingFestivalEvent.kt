package com.github.itsempa.nautilus.data.fishingevents

import at.hannibal2.skyhanni.data.ElectionApi
import at.hannibal2.skyhanni.data.ElectionCandidate
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.utils.SkyBlockTime
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusUtils.skyblockDays
import com.github.itsempa.nautilus.utils.NautilusUtils.skyblockMonths
import com.github.itsempa.nautilus.utils.TimePeriod
import com.github.itsempa.nautilus.utils.TimePeriod.Companion.getCurrentOrNext
import kotlin.time.Duration

@Module
data object FishingFestivalEvent : FishingEvent("FISHING_FESTIVAL") {
    override val name: String = "Fishing Festival"
    override val duration: Duration = 3.skyblockDays

    override fun updateNextTimePeriod(): TimePeriod? {
        val startYear = ElectionApi.nextMayorTimestamp.toSkyBlockTime().year - 1
        val list = buildList {
            fun add(time: SkyBlockTime) = add(TimePeriod(time, time + duration))
            if (Perk.EXTRA_EVENT_FISHING.isActive) add(SkyBlockTime(startYear, 6, 22))
            if (Perk.FISHING_FESTIVAL.isActive) {
                if (!ElectionCandidate.JERRY.isActive()) {
                    val first = SkyBlockTime(startYear, 4, 1)
                    add(first)
                    for (i in 1..11) {
                        add(first + (1.skyblockMonths * i))
                    }
                } else {
                    // TODO: deal with Perkpocalypse fishing festivals
                }
            }
        }
        return list.getCurrentOrNext()
    }
}
