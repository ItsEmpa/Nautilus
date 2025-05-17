package com.github.itsempa.nautilus.mixins.hooks

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.deps.moulconfig.gui.GuiScreenElementWrapper
import com.github.itsempa.nautilus.config.ConfigManager

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

}
