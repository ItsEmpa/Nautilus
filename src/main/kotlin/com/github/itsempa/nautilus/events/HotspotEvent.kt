package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import com.github.itsempa.nautilus.data.HotspotApi

sealed class HotspotEvent(val hotspot: HotspotApi.Hotspot) : SkyHanniEvent() {
    class Detected(hotspot: HotspotApi.Hotspot) : HotspotEvent(hotspot)
    class Removed(hotspot: HotspotApi.Hotspot) : HotspotEvent(hotspot)

    // TODO: implement
    class Seen(hotspot: HotspotApi.Hotspot) : HotspotEvent(hotspot)
}
