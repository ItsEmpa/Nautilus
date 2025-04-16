package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import com.github.itsempa.nautilus.data.VanquisherApi

abstract class VanquisherEvent : SkyHanniEvent() {
    class DeSpawn(val data: VanquisherApi.VanquisherData) : VanquisherEvent()
    class Death(val data: VanquisherApi.VanquisherData) : VanquisherEvent()
    class Spawn(val data: VanquisherApi.VanquisherData) : VanquisherEvent()
    object OwnSpawn : VanquisherEvent()
}


