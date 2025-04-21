package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.config.misc.SpinConfig
import com.google.gson.annotations.Expose

class MiscConfig {

    @Expose
    @ConfigOption(name = "Spin", desc = "")
    val spin: SpinConfig = SpinConfig()

}
