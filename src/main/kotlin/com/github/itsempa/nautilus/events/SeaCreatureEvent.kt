package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import com.github.itsempa.nautilus.data.SeaCreatureData

/** When a sea creature dies, both DeSpawn and Death events get called */
abstract class SeaCreatureEvent(val seaCreature: SeaCreatureData) : SkyHanniEvent() {
    class Spawn(seaCreature: SeaCreatureData) : SeaCreatureEvent(seaCreature)
    class DeSpawn(seaCreature: SeaCreatureData, val forced: Boolean) : SeaCreatureEvent(seaCreature)
    class Death(seaCreature: SeaCreatureData) : SeaCreatureEvent(seaCreature)

    inline val isOwn: Boolean get() = seaCreature.isOwn
    inline val isRare: Boolean get() = seaCreature.isRare
}
