package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import com.github.itsempa.nautilus.data.HotspotApi
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusRenderUtils.drawBoundingBox
import java.awt.Color

@Module(devOnly = true)
object HotspotHighlight {

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        for (hotspot in HotspotApi.hotspots) {
            val color = Color.BLUE
            val center = hotspot.center
            @Suppress("DEPRECATION")
            event.drawBoundingBox(hotspot.getAABB(), color, wireframe = true, throughBlocks = true)
            event.drawString(center, HotspotApi.HOTSPOT_NAMETAG, seeThroughBlocks = true)
        }
    }

    private val pos = Position(-300, 100)

    @HandleEvent
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        val result = HotspotApi.isHotspotFishing()
        pos.renderString("isHotspotFishing: $result", posLabel = "Hotspot Highlight")
    }

}
