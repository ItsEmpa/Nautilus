package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import com.github.itsempa.nautilus.data.HotspotApi

sealed class HotspotEvent(val hotspot: HotspotApi.Hotspot) : SkyHanniEvent() {
    class Detected(hotspot: HotspotApi.Hotspot) : HotspotEvent(hotspot)

    // TODO: maybe add a "reason" for the removal in the event?
    class Removed(hotspot: HotspotApi.Hotspot) : HotspotEvent(hotspot)

    class BuffFound(hotspot: HotspotApi.Hotspot) : HotspotEvent(hotspot)
    class Seen(hotspot: HotspotApi.Hotspot) : HotspotEvent(hotspot)

    // This gets called when the player starts fishing at a hotspot
    class StartFishing(hotspot: HotspotApi.Hotspot) : HotspotEvent(hotspot)
}
