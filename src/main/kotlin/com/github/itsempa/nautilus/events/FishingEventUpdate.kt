package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.GenericSkyHanniEvent
import com.github.itsempa.nautilus.data.fishingevents.FishingEvent

// TODO: confirm that the generic works properly
sealed class FishingEventUpdate<T : FishingEvent>(val event: T) : GenericSkyHanniEvent<T>(event.javaClass) {
    class Start<T : FishingEvent>(event: T) : FishingEventUpdate<T>(event)
    class End<T : FishingEvent>(event: T) : FishingEventUpdate<T>(event)
}
