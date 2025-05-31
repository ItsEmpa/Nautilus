package com.github.itsempa.nautilus.config.gui

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorSlider
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigLink
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.config.misc.SoundConfig
import com.google.gson.annotations.Expose

class BetterFishingTimerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "An improved version of §eSkyHanni§7's fishing timer.")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Warn Personal Cap", desc = "Warns you when you reach your personal sea creature cap.")
    @ConfigEditorBoolean
    var warnPersonalCap: Boolean = true

    @Expose
    @ConfigOption(name = "Warn Global Cap", desc = "Warns you when you reach the global sea creature cap if all mobs are in range.")
    @ConfigEditorBoolean
    var warnGlobalCap: Boolean = true

    @Expose
    @ConfigOption(name = "Time Alert", desc = "Warns you when the fishing timer reaches a certain value.")
    @ConfigEditorBoolean
    var timeAlert: Boolean = true

    @Expose
    @ConfigOption(name = "Time Alert Seconds", desc = "The time in seconds to alert you at.\n" +
        "§cNote: (sea creatures despawn after 6 minutes, aka 360s).")
    @ConfigEditorSlider(minValue = 240f, maxValue = 360f, minStep = 1f)
    var timeAlertSeconds: Int = 300

    @Expose
    @ConfigOption(name = "Sound", desc = "")
    @Accordion
    val sound = SoundConfig()

    @Expose
    @ConfigLink(owner = BetterFishingTimerConfig::class, field = "enabled")
    val position = Position(200, -100, centerX = true)

}
