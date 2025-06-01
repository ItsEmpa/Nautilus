package com.github.itsempa.nautilus.config.core

import at.hannibal2.skyhanni.deps.moulconfig.managed.ManagedConfig
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.config.Features
import com.github.itsempa.nautilus.config.core.loader.ConfigMapper
import com.github.itsempa.nautilus.features.misc.update.ConfigVersionDisplay
import com.github.itsempa.nautilus.features.misc.update.NautilusGuiOptionEditorUpdateCheck
import java.io.File

object ConfigManager {

    val managedConfig = ManagedConfig.create(File(Nautilus.directory, "config.json"), Features::class.java) {
        customProcessor(ConfigVersionDisplay::class.java) { processor, _ ->
            NautilusGuiOptionEditorUpdateCheck(processor)
        }
        mapper = ConfigMapper
        throwOnFailure()
    }

    fun save() = managedConfig.saveToFile()

    val editor by lazy(managedConfig::getEditor)

}
