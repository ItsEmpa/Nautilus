package com.github.itsempa.nautilus.config.gui

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigLink
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class LegionBobbinConfig {

    @Expose
    @ConfigOption(name = "Enable", desc = "Show the current Legion and Bobbin' Time buff.")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Hide without enchant", desc = "Hide the gui when you aren't wearing armor with Legion or Bobbin' on it.")
    @ConfigEditorBoolean
    var hideWithoutEnchant: Boolean = true

    @Expose
    @ConfigOption(name = "Always Show Max", desc = "Always show the buffs as if you were wearing max Legion/Bobbin' Time.")
    @ConfigEditorBoolean
    var alwaysShowMax: Boolean = false

    @Expose
    @ConfigLink(owner = LegionBobbinConfig::class, field = "enabled")
    val position: Position = Position(100, 100)

}
