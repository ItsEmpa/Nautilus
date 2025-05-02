package com.github.itsempa.nautilus.config.gui

import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorSlider
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.config.misc.SoundConfig
import com.google.gson.annotations.Expose

class CameraMoveConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Warn when you haven't moved the camera in a certain amount of catches.")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Catches", desc = "How many catches you need to make before the warning is shown.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 8f, minStep = 1f)
    var catches: Int = 5

    @Expose
    @ConfigOption(name = "Sound", desc = "")
    @Accordion
    val sound: SoundConfig = SoundConfig()

}
