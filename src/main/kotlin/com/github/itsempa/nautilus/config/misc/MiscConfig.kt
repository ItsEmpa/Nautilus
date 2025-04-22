package com.github.itsempa.nautilus.config.misc

import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class MiscConfig {

    @Expose
    @ConfigOption(name = "Spin", desc = "")
    @Accordion
    val spin: SpinConfig = SpinConfig()

    @Expose
    @ConfigOption(name = "Camera Move Warning", desc = "")
    @Accordion
    val cameraMove: CameraMoveConfig = CameraMoveConfig()
}
