package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.features.misc.update.ConfigVersionDisplay
import com.google.gson.annotations.Expose

class About {
    @Suppress("unused")
    @Transient
    @ConfigOption(name = "CurrentVersion", desc = "This is the ${Nautilus.MOD_NAME} version you are currently using.")
    @ConfigVersionDisplay
    val currentVersion: Unit = Unit

    @Expose
    @ConfigOption(name = "Notify Updates", desc = "Notify in chat when a new update is available.")
    @ConfigEditorBoolean
    var notifyUpdates: Boolean = true

    @Expose
    @ConfigOption(name = "Full Auto Updates", desc = "Automatically download an update when detected.")
    @ConfigEditorBoolean
    var autoUpdates: Boolean = false

    @Expose
    @ConfigOption(name = "Debug", desc = "Shows debug information in chat, useful for detecting errors.")
    @ConfigEditorBoolean
    var debug: Boolean = false
}
