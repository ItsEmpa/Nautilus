package com.github.itsempa.nautilus.config.gui

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorSlider
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorText
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigLink
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.utils.NautilusUtils.asProperty
import com.google.gson.annotations.Expose

class HealthDisplayConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Shows a GUI with the health of the sea creatures shown.")
    @ConfigEditorBoolean
    var enabled = false

    @Expose
    @ConfigOption(name = "Health Display Mobs", desc = "The name of the sea creatures to show the health display for, separated by commas.")
    @ConfigEditorText
    val names = "Lord Jawbus, Thunder".asProperty()

    @Expose
    @ConfigOption(name = "Limit", desc = "The maximum amount of mobs to show.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 10f, minStep = 1f)
    var limit = 5

    @Expose
    @ConfigLink(owner = HealthDisplayConfig::class, field = "enabled")
    val pos = Position(200, 200, centerX = true)
}
