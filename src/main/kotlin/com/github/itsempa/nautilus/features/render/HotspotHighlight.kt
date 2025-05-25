package com.github.itsempa.nautilus.features.render

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.HotspotApi
import com.github.itsempa.nautilus.utils.NautilusRenderUtils.drawCircle
import me.owdding.ktmodules.Module

@Module
object HotspotHighlight {

    private val config get() = Nautilus.feature.render

    fun shouldHideHotspotParticles(): Boolean = config.hideHotspotParticles

    @HandleEvent(onlyOnIslands = [IslandType.HUB, IslandType.SPIDER_DEN, IslandType.BACKWATER_BAYOU, IslandType.CRIMSON_ISLE])
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!config.hotspotHighlight) return
        for (hotspot in HotspotApi.hotspots) {
            val center = hotspot.center
            val buff = hotspot.buff ?: HotspotApi.HotspotBuff.UNKNOWN
            val color = buff.color.addAlpha(127)
            event.drawCircle(center, hotspot.radius, color)
        }
    }

}
