package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import com.github.itsempa.nautilus.data.categories.FishingCategory
import com.github.itsempa.nautilus.modules.Module

@Module(devOnly = true)
object FishingCategoryDisplay {

    private val pos = Position(100, 200)

    @HandleEvent
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        pos.renderStrings(
            listOf(
                "Current Category: ${FishingCategory.activeCategory?.internalName ?: "None"}",
                "Extra Categories: ${FishingCategory.extraCategories.map { it.internalName }.ifEmpty { "None" }}",
            ),
            posLabel = "Fishing Categories"
        )
    }

}
