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
    @ConfigOption(
        name = "Lootshare Range",
        desc = "Shows the range at which you can lootshare. The range is §aGreen §7when the mob is your own or" +
            "when inside the lootshare range."
    )
    @ConfigEditorBoolean
    var lootshareRange = false

    @ConfigOption(name = "", desc = ".")
    @ConfigEditorButton(buttonText = "")
    val : Runnable = Runnable {  }

    @Expose
    @ConfigOption(name = "Lootshare Mobs", desc = "The name of the sea creatures to show the lootshare range for, separated by commas.")
    @ConfigEditorText
    val lootshareMobs = "Lord Jawbus, Thunder".asProperty()

    @Expose
    @ConfigOption(name = "Fix Lava Bobbers", desc = "Makes bobbers not sink in lava.")
    @ConfigEditorBoolean
    var fixLavaBobbers = true


}
