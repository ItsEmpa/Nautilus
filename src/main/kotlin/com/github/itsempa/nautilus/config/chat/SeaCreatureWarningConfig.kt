package com.github.itsempa.nautilus.config.chat

import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.config.misc.SoundConfig
import com.google.gson.annotations.Expose

class SeaCreatureWarningConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show a title when you fish a rare sea creature.")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Party message", desc = "Send a message in party chat when you catch a rare sea creature.")
    @ConfigEditorBoolean
    var partyMessage: Boolean = true

    @Expose
    @ConfigOption(name = "Other Sea Creatures", desc = "Show a title when other people fish up a rare sea creature.")
    @ConfigEditorBoolean
    var otherSeaCreatures: Boolean = true

    @Expose
    @ConfigOption(name = "Sound", desc = "")
    @Accordion
    val sound: SoundConfig = SoundConfig()
}
