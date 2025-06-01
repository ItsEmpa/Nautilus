package com.github.itsempa.nautilus.features.render

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.config.core.loader.NautilusConfigFixEvent
import com.github.itsempa.nautilus.data.HotspotApi
import com.github.itsempa.nautilus.utils.NautilusRenderUtils.drawCircle
import me.owdding.ktmodules.Module

@Module
object HotspotHighlight {

    private val config get() = Nautilus.feature.render.hotspotHighlight

    fun shouldHideHotspotParticles(): Boolean = config.hideParticles

    @HandleEvent(onlyOnIslands = [IslandType.HUB, IslandType.SPIDER_DEN, IslandType.BACKWATER_BAYOU, IslandType.CRIMSON_ISLE])
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!config.enabled) return
        val alpha = config.transparency
        for (hotspot in HotspotApi.hotspots) {
            val center = hotspot.center
            val buff = hotspot.buff ?: HotspotApi.HotspotBuff.UNKNOWN
            val color = buff.color.addAlpha(alpha)
            event.drawCircle(center, hotspot.radius, color)
        }
    }

    @HandleEvent
    fun onConfigFix(event: NautilusConfigFixEvent) {
        event.move(0, "render.hotspotHighlight", "render.hotspotHighlight.enabled")
        event.move(0, "render.hideHotspotParticles", "render.hotspotHighlight.hideParticles")
    }

}
