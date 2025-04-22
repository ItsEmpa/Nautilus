package com.github.itsempa.nautilus.config.render

import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorText
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.utils.NautilusUtils.asProperty
import com.google.gson.annotations.Expose

class RenderConfig {

    @Expose
    @ConfigOption(name = "Invincibility Timer", desc = "Show a timer on top of sea creatures and Vanquishers.")
    @ConfigEditorBoolean
    var invincibility = false

    @Expose
    @ConfigOption(name = "Lootshare Range", desc = "Shows the range at which you can lootshare.")
    @ConfigEditorBoolean
    var lootshareRange = false

    @Expose
    @ConfigOption(name = "Lootshare Mobs", desc = "The name of the sea creatures to show the lootshare range for, separated by commas.")
    @ConfigEditorText
    val lootshareMobs = "Lord Jawbus, Thunder".asProperty()


}
