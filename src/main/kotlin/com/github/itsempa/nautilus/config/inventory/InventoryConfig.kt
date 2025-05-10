package com.github.itsempa.nautilus.config.inventory

import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class InventoryConfig {

    @Expose
    @ConfigOption(name = "Hide Book of Stats", desc = "Hides the text in items that shows the kills with book of stats.")
    @ConfigEditorBoolean
    var hideBookOfStats: Boolean = false

}
