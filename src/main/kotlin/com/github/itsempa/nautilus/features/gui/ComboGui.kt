package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.ComboData
import com.github.itsempa.nautilus.events.combo.ComboUpdateEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.fullEnumMapOf

@Module
object ComboGui {

    private val config get() = Nautilus.feature.gui

    private var display: String = update(0, 'f', fullEnumMapOf<ComboData.ComboBuff, Int>(0))

    @HandleEvent
    fun onComboUpdate(event: ComboUpdateEvent) {
        with(event) {
            display = update(combo, colorCode, buffs)
        }
    }

    private fun update(combo: Int, colorCode: Char, buffs: Map<ComboData.ComboBuff, Int>): String {
        return "§$colorCode§l+$combo §r${buffs.entries.joinToString(" ") { (buff, amount) -> buff.format(amount) }}"
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.comboGui) return
        config.comboGuiPos.renderString(display, posLabel = "Combo Gui")
    }

}
