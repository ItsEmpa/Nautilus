package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import com.github.itsempa.nautilus.data.SeaCreatureData
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusRenderUtils.drawBoundingBox
import java.awt.Color

@Module(devOnly = true)
object SeaCreatureHighlight {

    private val seaCreatures = mutableSetOf<SeaCreatureData>()

    @HandleEvent
    fun onSeaCreatureSpawn(event: SeaCreatureEvent.Spawn) = seaCreatures.add(event.seaCreature)

    @HandleEvent
    fun onSeaCreatureDeath(event: SeaCreatureEvent.Death) = seaCreatures.remove(event.seaCreature)

    @HandleEvent
    fun onSeaCreatureDeSpawn(event: SeaCreatureEvent.DeSpawn) {
        if (event.forced) seaCreatures.remove(event.seaCreature)
    }

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
