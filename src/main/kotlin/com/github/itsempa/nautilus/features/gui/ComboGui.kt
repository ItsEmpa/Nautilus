package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.ComboData
import com.github.itsempa.nautilus.events.combo.ComboUpdateEvent
import com.github.itsempa.nautilus.modules.Module

@Module
object ComboGui {

    private val config get() = Nautilus.feature.gui

    private var display: String? = null

    @HandleEvent
    fun onComboUpdate(event: ComboUpdateEvent) {
        with(event) {
            display = update(combo, colorCode, buffs)
        }
    }

    private fun update(combo: Int, colorCode: Char, buffs: Map<ComboData.ComboBuff, Int>): String? {
        if (combo == 0) return null
        return "§$colorCode§l+$combo §r${buffs.entries.joinToString(" ") { (buff, amount) -> buff.format(amount) }}"
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.comboGui) return
        config.comboGuiPos.renderString(display, posLabel = "Combo Gui")
    }

}
