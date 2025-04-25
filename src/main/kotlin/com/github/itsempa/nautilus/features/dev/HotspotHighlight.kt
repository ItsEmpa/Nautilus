package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import com.github.itsempa.nautilus.data.HotspotApi
import com.github.itsempa.nautilus.events.HotspotEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusRenderUtils.drawBoundingBox
import java.awt.Color

@Module(devOnly = true)
object HotspotHighlight {

    private val hotspots = mutableSetOf<HotspotApi.Hotspot>()

    @HandleEvent
    fun onHotspotDetect(event: HotspotEvent.Detected) = hotspots.add(event.hotspot)

    @HandleEvent
    fun onHotspotRemove(event: HotspotEvent.Removed) = hotspots.remove(event.hotspot)

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        for (hotspot in hotspots) {
            val color = Color.BLUE
            val center = hotspot.center
            @Suppress("DEPRECATION")
            event.drawBoundingBox(hotspot.aabb, color, wireframe = true, throughBlocks = true)
            event.drawString(center, HotspotApi.HOTSPOT_NAMETAG, seeThroughBlocks = true)
        }
    }

}
