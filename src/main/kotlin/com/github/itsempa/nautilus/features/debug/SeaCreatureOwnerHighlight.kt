package com.github.itsempa.nautilus.features.debug

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.RenderEntityOutlineEvent
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.MobUtils.mob
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.SeaCreatureDetectionApi.seaCreature
import me.owdding.ktmodules.Module
import net.minecraft.entity.Entity
import java.awt.Color

@Module
object SeaCreatureOwnerHighlight {

    private val config get() = Nautilus.feature.dev.highlightSeaCreatures

    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.HIGHEST)
    fun onRenderOutline(event: RenderEntityOutlineEvent) {
        if (event.type != RenderEntityOutlineEvent.Type.NO_XRAY || !isEnabled()) return
        event.queueEntitiesToOutline(::outlineEntity)
    }

    private fun outlineEntity(entity: Entity): Color? {
        val seaCreature = entity.mob?.seaCreature ?: return null
        val color = if (seaCreature.isOwn) LorenzColor.LIGHT_PURPLE else LorenzColor.BLUE
        return color.toColor()
    }

    fun isEnabled(): Boolean = config

}
