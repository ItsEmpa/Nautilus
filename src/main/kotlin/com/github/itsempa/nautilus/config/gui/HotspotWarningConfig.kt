package com.github.itsempa.nautilus.config.gui

import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.config.misc.SoundConfig
import com.google.gson.annotations.Expose

class HotspotWarningConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Warns you when the hotspot you are currently fishing at disappears.")
    @ConfigEditorBoolean
    var enabled: Boolean = true

    @Expose
    @ConfigOption(name = "Sound", desc = "")
    @Accordion
    val sound = SoundConfig()
}
