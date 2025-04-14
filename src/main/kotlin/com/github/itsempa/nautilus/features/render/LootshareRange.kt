package com.github.itsempa.nautilus.features.render

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawSphereWireframeInWorld
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.SeaCreatureApi
import com.github.itsempa.nautilus.data.SeaCreatureData
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusUtils.toSet

@Module
object LootshareRange {
    private val config get() = Nautilus.feature.render

    const val RANGE = 30.0f // TODO: get correct range

    private var names = setOf<String>()
    private val seaCreatures = mutableSetOf<SeaCreatureData>()

    @HandleEvent
    fun onSeaCreatureSpawn(event: SeaCreatureEvent.Spawn) = addMob(event.seaCreature)

    @HandleEvent
    fun onSeaCreatureDeSpawn(event: SeaCreatureEvent.DeSpawn) {
        if (event.forced) seaCreatures.remove(event.seaCreature)
    }

    @HandleEvent
    fun onSeaCreatureDeath(event: SeaCreatureEvent.Death) = seaCreatures.remove(event.seaCreature)

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!config.lootshareRange) return
        for (seaCreature in seaCreatures) {
            if (!seaCreature.isLoaded()) continue
            val pos = seaCreature.pos ?: continue
            val color = if (pos.distanceToPlayer() < RANGE) LorenzColor.GREEN else LorenzColor.WHITE
            event.drawSphereWireframeInWorld(color.toColor(), pos, RANGE)
        }
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        updateNames()
        config.lootshareMobs.onToggle {
            updateNames()
            reloadMobs()
        }
    }

    private fun addMob(seaCreature: SeaCreatureData) {
        if (seaCreature.name in names) seaCreatures.add(seaCreature)
    }

    private fun reloadMobs() {
        seaCreatures.clear()
        SeaCreatureApi.getSeaCreatures().forEach(::addMob)
    }

    private fun updateNames() {
        names = config.lootshareMobs.toSet(false)
    }
}
