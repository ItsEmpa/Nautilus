package com.github.itsempa.nautilus.config.chat

import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorDraggableList
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.config.misc.SoundConfig
import com.github.itsempa.nautilus.data.HotspotApi
import com.google.gson.annotations.Expose

class HotspotSharingConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show a clickable chat message to share hotspots, and show a title when someone shares one.")
    @ConfigEditorBoolean
    var enabled = true

    @Expose
    @ConfigOption(name = "Hotspot Buffs", desc = "Choose what hotspot buffs to warn about and share, and the priority of them.")
    @ConfigEditorDraggableList
    val buffs: MutableList<HotspotApi.HotspotBuff> = HotspotApi.HotspotBuff.default

    @Expose
    @ConfigOption(name = "All Chat", desc = "Add option to send the message in all chat.")
    @ConfigEditorBoolean
    var allChat = false

    @Expose
    @ConfigOption(name = "Instantly Send Party", desc = "Automatically send message in party chat when the hotspot is detected.")
    @ConfigEditorBoolean
    var instantPartyChat = false

    @Expose
    @ConfigOption(name = "Sound", desc = "")
    @Accordion
    val sound = SoundConfig()

}
