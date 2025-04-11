package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.deps.moulconfig.managed.ManagedConfig
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.utils.KotlinTypeAdapterFactory
import at.hannibal2.skyhanni.utils.json.SkyHanniTypeAdapters
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.features.misc.update.ConfigVersionDisplay
import com.github.itsempa.nautilus.features.misc.update.GuiOptionEditorUpdateCheck
import com.github.itsempa.nautilus.features.misc.update.SemVersion
import com.github.itsempa.nautilus.modules.Module
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import java.io.File

@Module
object ConfigManager {

    val managedConfig = ManagedConfig.create(File("config/${Nautilus.MOD_ID}/config.json"), Features::class.java) {
        customProcessor(ConfigVersionDisplay::class.java) { processor, _ ->
            GuiOptionEditorUpdateCheck(processor)
        }
        jsonMapper {
            gsonBuilder.setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .registerTypeAdapterFactory(KotlinTypeAdapterFactory())
                .enableComplexMapKeySerialization()
                .addSkyHanniTypeAdapters()
                .registerTypeAdapter(SemVersion.TYPE_ADAPTER)
        }
        throwOnFailure()
    }

    fun save() = managedConfig.saveToFile()

    fun getEditor() = managedConfig.getEditor()

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (event.repeatSeconds(60)) save()
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) = save()

    private inline fun <reified T> GsonBuilder.registerTypeAdapter(adapter: TypeAdapter<T>, nullSafe: Boolean = true): GsonBuilder {
        val newAdapter = if (nullSafe) adapter.nullSafe() else adapter
        return registerTypeAdapter(T::class.java, newAdapter)
    }

    private fun GsonBuilder.addSkyHanniTypeAdapters(): GsonBuilder =
        registerTypeAdapter(SkyHanniTypeAdapters.UUID)
            .registerTypeAdapter(SkyHanniTypeAdapters.VEC_STRING)
            .registerTypeAdapter(SkyHanniTypeAdapters.TROPHY_RARITY)
            .registerTypeAdapter(SkyHanniTypeAdapters.NEU_ITEMSTACK)
            .registerTypeAdapter(SkyHanniTypeAdapters.INTERNAL_NAME)
            .registerTypeAdapter(SkyHanniTypeAdapters.RARITY)
            .registerTypeAdapter(SkyHanniTypeAdapters.ISLAND_TYPE)
            .registerTypeAdapter(SkyHanniTypeAdapters.MOD_VERSION)
            .registerTypeAdapter(SkyHanniTypeAdapters.TRACKER_DISPLAY_MODE)
            .registerTypeAdapter(SkyHanniTypeAdapters.TIME_MARK)
            .registerTypeAdapter(SkyHanniTypeAdapters.DURATION)
            .registerTypeAdapter(SkyHanniTypeAdapters.LOCALE_DATE)

}
