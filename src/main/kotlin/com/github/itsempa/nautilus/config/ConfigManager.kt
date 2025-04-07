package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.deps.moulconfig.managed.GsonMapper
import at.hannibal2.skyhanni.deps.moulconfig.managed.ManagedConfig
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.features.misc.update.ConfigVersionDisplay
import com.github.itsempa.nautilus.features.misc.update.GuiOptionEditorUpdateCheck
import com.github.itsempa.nautilus.modules.Module
import java.io.File

@Module
object ConfigManager {

    val managedConfig = ManagedConfig.create(File("config/${Nautilus.MOD_ID}/config.json"), Features::class.java) {
        customProcessor(ConfigVersionDisplay::class.java) { processor, _ ->
            GuiOptionEditorUpdateCheck(processor)
        }
        this.throwOnFailure()
        val mapper = this.mapper as GsonMapper<Features>
        mapper.gsonBuilder.setPrettyPrinting()
    }

    fun save() = managedConfig.saveToFile()

    fun getEditor() = managedConfig.getEditor()

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (event.repeatSeconds(60)) save()
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) = save()

}
