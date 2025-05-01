package com.github.itsempa.nautilus.config.gui

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigLink
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class RareDropsTrackerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable showing how many sea creatures it has been since you acquired certin rare drops.")
    @ConfigEditorBoolean
    var enabled: Boolean = true

    @Expose
    @ConfigOption(name = "Send Chat Message", desc = "Send a chat message whenever you get the items.")
    @ConfigEditorBoolean
    var sendChatMessage: Boolean = true

    @Expose
    @ConfigLink(owner = RareDropsTrackerConfig::class, field = "enabled")
    val position = Position(-300, 200)
}
