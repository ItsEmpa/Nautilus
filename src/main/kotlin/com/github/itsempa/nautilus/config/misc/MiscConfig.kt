package com.github.itsempa.nautilus.config.misc

import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class MiscConfig {

    @Expose
    @ConfigOption(name = "Spin", desc = "")
    @Accordion
    val spin: SpinConfig = SpinConfig()

    @Expose
    @ConfigOption(name = "Hide Armor Glint", desc = "Hide the enchantment glint on armor pieces.")
    @ConfigEditorBoolean
    var hideArmorGlint: Boolean = false
}
