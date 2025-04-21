package com.github.itsempa.nautilus.config.misc

import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorSlider
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class SpinConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "spin really fast!! weeeeeeee")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Spin Speed", desc = "How fast you spin")
    @ConfigEditorSlider(minValue = -100f, maxValue = 100f, minStep = 1f)
    var spinSpeed: Int = 50
}
