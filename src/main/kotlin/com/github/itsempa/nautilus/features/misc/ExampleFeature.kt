package com.github.itsempa.nautilus.features.misc

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.modules.Module

@Module
object ExampleFeature {

    private val config get() = Nautilus.feature.exampleCategory

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.exampleOption) return
        config.position.renderString("Hi, this is a test feature", posLabel = "Example Option")
    }

}
