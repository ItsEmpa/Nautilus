package com.github.itsempa.nautilus.data.fishingevents

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.github.itsempa.nautilus.utils.NautilusUtils.minBy
import com.github.itsempa.nautilus.utils.NautilusUtils.orFalse
import com.github.itsempa.nautilus.utils.TimePeriod

// WIP
/**
 * TODO: add this logic to fishing festival
 *
 *         override fun MutableList<FishingEvent>.addAll() {
 *             val startYear = ElectionApi.nextMayorTimestamp.toSkyBlockTime().year - 1
 *
 *             fun add(time: SkyBlockTime) = add(FishingFestival(time))
 *             if (Perk.EXTRA_EVENT_FISHING.isActive) add(SkyBlockTime(startYear, 6, 22))
 *
 *             if (Perk.FISHING_FESTIVAL.isActive) {
 *                 if (!ElectionCandidate.JERRY.isActive()) {
 *                     for (i in 1..12) {
 *                         val year = if (i < 4) startYear + 1 else startYear
 *                         add(SkyBlockTime(year, i, 1))
 *                     }
 *                 } else {
 *                     // deal with Perkpocalypse fishing festivals
 *                 }
 *             }
 *         }
 */

abstract class FishingEvent(val internalName: String) {
    abstract val name: String

    var isActive: Boolean = false
        private set

    /** Current event's time period if active, or next event's */
    var timePeriod: TimePeriod? = null
        private set

    val startTime: SimpleTimeMark get() = timePeriod?.start ?: SimpleTimeMark.farFuture()
    val endTime: SimpleTimeMark get() = timePeriod?.end ?: SimpleTimeMark.farPast()

    private var overrideTimePeriods = emptyList<TimePeriod>()
    private var nextUpdate = SimpleTimeMark.farPast()

    /** Updates the [timePeriod] of the event to be the next event (or null if there isn't a next one). */
    abstract fun updateNextTimePeriod(): TimePeriod?

    init {
        @Suppress("LeakingThis")
        events.add(this)
    }

    // TODO: Handle updating next timePeriod, mayor rotation/first detection, and repo overrides
    private fun onSecondPassed() {
        if (nextUpdate.isInFuture()) return
        val isNow = timePeriod?.isNow().orFalse()
        if (isNow == isActive) return
        isActive = isNow
        if (!isActive) internalUpdateTimePeriod()
    }

    private fun internalUpdateTimePeriod() {
        val override = overrideTimePeriods.filter { it.isNow() || it.isInFuture() }.minByOrNull { it.timeUntilStart() }
        val next = updateNextTimePeriod()
        timePeriod = when {
            override == null -> next
            next == null -> override
            else -> minBy(next, override) {
                if (it.isNow()) it.end else it.start
            }
        }
    }

    //@Module
    companion object {
        private val events = mutableListOf<FishingEvent>()

        @HandleEvent
        fun onSecondPassed(event: SecondPassedEvent) = events.forEach(FishingEvent::onSecondPassed)
    }
}
