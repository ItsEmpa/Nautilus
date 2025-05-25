package com.github.itsempa.nautilus.features.inventory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.utils.RegexUtils.find
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasBookOfStats
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.utils.removeRange
import me.owdding.ktmodules.Module

@Module
object HideBookOfStats {

    private val config get() = Nautilus.feature.inventory.hideBookOfStats

    private val pattern = "^§5§o§fKills:".toPattern()

    @HandleEvent(onlyOnSkyblock = true)
    fun onTooltip(event: ToolTipEvent) {
        if (!config) return
        val stack = event.itemStack
        if (!stack.hasBookOfStats()) return
        val index = event.toolTip.indexOfLast { pattern.find(it) }
        if (index == -1) return
        event.toolTip.removeRange(index..index + 1)
    }

}
