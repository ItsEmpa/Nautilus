package com.github.itsempa.nautilus.config.gui

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorSlider
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigLink
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.features.gui.ComboGui
import com.google.gson.annotations.Expose

class ComboGuiConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Shows a simple gui with the current combo and current combo buffs.")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(
        name = "Timer",
        desc = "Shows a timer for when the combo will run out.\n" +
            "§cRequires having a Book of Stats in your weapon."
    )
    @ConfigEditorBoolean
    var timer: Boolean = false

    @Expose
    @ConfigOption(name = "Grandma Wolf Level", desc = "Specify what level your grandma wolf is at.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 100f, minStep = 1f)
    var grandmaWolfLevel: Int = 100

    @Expose
    @ConfigLink(owner = ComboGui::class, field = "enabled")
    val pos = Position(-300, 150)

}
