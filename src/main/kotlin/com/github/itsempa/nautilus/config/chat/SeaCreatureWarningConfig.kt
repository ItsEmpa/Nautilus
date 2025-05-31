package com.github.itsempa.nautilus.config.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorButton
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import at.hannibal2.skyhanni.deps.moulconfig.observer.Property
import at.hannibal2.skyhanni.utils.ConfigUtils.jumpToEditor
import com.github.itsempa.nautilus.config.misc.SoundConfig
import com.github.itsempa.nautilus.utils.NautilusUtils.asProperty
import com.google.gson.annotations.Expose

class SeaCreatureWarningConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show a title when you fish a rare sea creature.")
    @ConfigEditorBoolean
    val enabled: Property<Boolean> = false.asProperty()

    @Expose
    @ConfigOption(name = "Party message", desc = "Send a message in party chat when you catch a rare sea creature.")
    @ConfigEditorBoolean
    var partyMessage: Boolean = true

    @Expose
    @ConfigOption(name = "Other Sea Creatures", desc = "Show a title when other people fish up a rare sea creature.")
    @ConfigEditorBoolean
    val otherSeaCreatures: Property<Boolean> = true.asProperty()

    @Expose
    @ConfigOption(name = "Sound", desc = "")
    @Accordion
    val sound: SoundConfig = SoundConfig()

    @Transient
    @ConfigOption(name = "Note", desc = "§cThis feature can clash with SkyHanni's rare catches config. Click here to go to its settings.")
    @ConfigEditorButton(buttonText = "Note")
    val skyhanniConfig: Runnable = Runnable {
        SkyHanniMod.feature.fishing.rareCatches::alertOwnCatches.jumpToEditor()
    }
}
