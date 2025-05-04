package com.github.itsempa.nautilus.features.debug

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.RenderEntityOutlineEvent
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.MobUtils.mob
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.SeaCreatureDetectionApi.seaCreature
import com.github.itsempa.nautilus.modules.Module
import net.minecraft.entity.Entity

@Module
object SeaCreatureOwnerHighlight {

    private val config get() = Nautilus.feature.dev.highlightSeaCreatures

    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.HIGHEST)
    fun onRenderOutline(event: RenderEntityOutlineEvent) {
        if (event.type != RenderEntityOutlineEvent.Type.NO_XRAY || !isEnabled()) return
        event.queueEntitiesToOutline(::outlineEntity)
    }

    private fun outlineEntity(entity: Entity): Int? {
        val seaCreature = entity.mob?.seaCreature ?: return null
        val color = if (seaCreature.isOwn) LorenzColor.LIGHT_PURPLE else LorenzColor.BLUE
        return color.toColor().rgb
    }

    fun isEnabled(): Boolean = config

}
