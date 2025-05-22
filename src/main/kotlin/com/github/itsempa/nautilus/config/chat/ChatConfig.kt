package com.github.itsempa.nautilus.config.chat

import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class ChatConfig {

    @Expose
    @ConfigOption(name = "Hotspot Sharing", desc = "")
    @Accordion
    val hotspotSharing: HotspotSharingConfig = HotspotSharingConfig()

    @Expose
    @ConfigOption(name = "Sea Creature Warning", desc = "")
    @Accordion
    val seaCreatureWarning: SeaCreatureWarningConfig = SeaCreatureWarningConfig()

    @Expose
    @ConfigOption(name = "Best Combo", desc = "Show in chat a message when you beat your PB for longest combo.")
    @ConfigEditorBoolean
    var bestCombo: Boolean = true

}
