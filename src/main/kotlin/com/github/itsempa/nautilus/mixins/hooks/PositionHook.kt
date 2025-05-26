package com.github.itsempa.nautilus.mixins.hooks

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.deps.moulconfig.gui.GuiScreenElementWrapper
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import com.github.itsempa.nautilus.config.ConfigManager
import com.github.itsempa.nautilus.mixins.hooks.NautilusPositionData.Companion.isNautilus
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

object PositionHook {

    @JvmStatic
    fun canJumpToNautilusConfig(pos: Position): Boolean {
        val editor = ConfigManager.editor
        val field = pos.linkField!! // This should never be null when called
        return editor.getOptionFromField(field) != null
    }

    @JvmStatic
    fun jumpToNautilusConfig(pos: Position) {
        val editor = ConfigManager.editor
        val field = pos.linkField ?: return
        val option = editor.getOptionFromField(field) ?: return
        editor.search("")
        if (!editor.goToOption(option)) return
        SkyHanniMod.screenToOpen = GuiScreenElementWrapper(editor)
    }

    @JvmStatic
    fun redirectHoverText(pos: Position, cir: CallbackInfoReturnable<List<String>>) {
        if (!pos.isNautilus) return
        cir.returnValue = listOf(
            "§cNautilus Position Editor",
            "§b${pos.internalName}",
            "  §7x: §e${pos.x}§7, y: §e${pos.y}§7, scale: §e${pos.scale.roundTo(2)}",
            "",
            "§eRight-Click to open associated config options!",
            "§eUse Scroll-Wheel to resize!",
        )
    }

}
