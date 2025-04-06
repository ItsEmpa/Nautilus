package com.example.features.misc

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import com.example.ExampleMod
import com.example.modules.Module

@Module
object ExampleFeature {

    private val config get() = ExampleMod.feature.exampleCategory

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.exampleOption) return
        config.position.renderString("Hi, this is a test feature", posLabel = "Example Option")
    }

}
