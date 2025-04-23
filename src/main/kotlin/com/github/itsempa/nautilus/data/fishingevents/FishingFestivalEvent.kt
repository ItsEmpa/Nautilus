package com.github.itsempa.nautilus.data.fishingevents

import at.hannibal2.skyhanni.data.ElectionApi
import at.hannibal2.skyhanni.data.ElectionCandidate
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.utils.SkyBlockTime
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.TimePeriod
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@Module
object FishingFestivalEvent : FishingEvent("FISHING_FESTIVAL") {
    override val name: String = "Fishing Festival"
    override val duration: Duration = 1.hours

    /** Updates the [timePeriod] of the event to be the next event (or null if there isn't a next one). */
    override fun updateNextTimePeriod(): TimePeriod? {
        val startYear = ElectionApi.nextMayorTimestamp.toSkyBlockTime().year - 1
        val list = buildList {
            fun add(time: SkyBlockTime) = add(TimePeriod(time, time + duration))
            if (Perk.EXTRA_EVENT_FISHING.isActive) add(SkyBlockTime(startYear, 6, 22))
            if (Perk.FISHING_FESTIVAL.isActive) {
                if (!ElectionCandidate.JERRY.isActive()) {
                    for (i in 1..12) {
                        val year = if (i < 4) startYear + 1 else startYear
                        add(SkyBlockTime(year, i, 1))
                    }
                } else {
                    // TODO: deal with Perkpocalypse fishing festivals
                }
            }
        }
        return list.filter { !it.isInPast() }.minByOrNull { if (it.isNow()) it.end else it.start }
    }

    override fun onStart() {
        NautilusChat.chat("Started Fishing Festival event")
    }

    override fun onEnd() {
        NautilusChat.chat("Ended Fishing Festival event")
    }
}
