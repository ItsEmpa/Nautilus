package com.github.itsempa.nautilus.features.debug

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import com.github.itsempa.nautilus.data.SeaCreatureData
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.modules.DevModule
import com.github.itsempa.nautilus.utils.NautilusRenderUtils.drawBoundingBox
import java.awt.Color

@DevModule
object SeaCreatureHighlight {

    private val seaCreatures = mutableSetOf<SeaCreatureData>()

    @HandleEvent
    fun onSeaCreatureSpawn(event: SeaCreatureEvent.Spawn) = seaCreatures.add(event.seaCreature)

    @HandleEvent
    fun onSeaCreatureDeSpawn(event: SeaCreatureEvent.Remove) = seaCreatures.remove(event.seaCreature)

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        for (data in seaCreatures) {
            val aabb = data.aabb ?: continue
            val color = if (data.isOwn) Color.GREEN else Color.BLUE

            event.drawBoundingBox(
                aabb,
                color,
                wireframe = true,
                throughBlocks = true,
            )
        }
    }

}
