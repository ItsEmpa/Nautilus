package com.github.itsempa.nautilus.data.fishingevents

import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.TimePeriod
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@Module
data object SpookyFestivalEvent : FishingEvent("SPOOKY_FESTIVAL") {
    override val name: String = "Spooky Festival"
    override val duration: Duration = 3.hours

    override fun updateNextTimePeriod(): TimePeriod? {
        // TODO: implement
        return null
    }
}
