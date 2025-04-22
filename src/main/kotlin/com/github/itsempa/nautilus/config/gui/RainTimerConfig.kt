package com.github.itsempa.nautilus.config.gui

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorSlider
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigLink
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.config.misc.SoundConfig
import com.google.gson.annotations.Expose

class RainTimerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show the time remaining until the rain/thunder ends.")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Warn on low", desc = "Show a warning when the time remaining is less than this (only works in The Park).")
    @ConfigEditorBoolean
    var warnOnLow: Boolean = false

    @Expose
    @ConfigOption(name = "Warning time", desc = "How many seconds before the rain ends to show the warning.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 60f, minStep = 1f)
    var warningTime: Int = 20

    @Expose
    @ConfigOption(name = "Sound", desc = "")
    @Accordion
    val sound: SoundConfig = SoundConfig()

    @Expose
    @ConfigLink(owner = RainTimerConfig::class, field = "enabled")
    val position: Position = Position(100, 100)
}
