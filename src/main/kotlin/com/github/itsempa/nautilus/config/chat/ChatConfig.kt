package com.github.itsempa.nautilus.config.chat

import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class ChatConfig {

    @Expose
    @ConfigOption(name = "Hotspot Sharing", desc = "")
    @Accordion
    val hotspotSharing: HotspotSharingConfig = HotspotSharingConfig()

}
