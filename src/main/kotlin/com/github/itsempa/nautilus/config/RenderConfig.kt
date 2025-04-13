package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class RenderConfig {

    // TODO: UNTESTED
    @Expose
    @ConfigOption(name = "Invincibility Timer", desc = "Show a timer on top of sea creatures and Vanquishers.")
    @ConfigEditorBoolean
    var invincibility = false
}
