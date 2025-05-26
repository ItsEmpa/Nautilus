package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.deps.moulconfig.LegacyStringChromaColourTypeAdapter
import at.hannibal2.skyhanni.deps.moulconfig.managed.ManagedConfig
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.utils.KotlinTypeAdapterFactory
import at.hannibal2.skyhanni.utils.json.BaseGsonBuilder
import at.hannibal2.skyhanni.utils.json.SkyHanniTypeAdapters
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.categories.FishingCategory
import com.github.itsempa.nautilus.data.core.EmptyFieldAdapterFactory
import com.github.itsempa.nautilus.data.fishingevents.FishingEvent
import com.github.itsempa.nautilus.features.misc.update.ConfigVersionDisplay
import com.github.itsempa.nautilus.features.misc.update.NautilusGuiOptionEditorUpdateCheck
import com.github.itsempa.nautilus.features.misc.update.SemVersion
import com.github.itsempa.nautilus.utils.TimePeriod
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import me.owdding.ktmodules.Module
import java.io.File

@Module
object ConfigManager {

    val gson: Gson = BaseGsonBuilder.gson().addNautilusTypeAdapters().create()

    val directory = File("config/${Nautilus.MOD_ID}")

    val managedConfig = ManagedConfig.create(File(directory, "config.json"), Features::class.java) {
        customProcessor(ConfigVersionDisplay::class.java) { processor, _ ->
            NautilusGuiOptionEditorUpdateCheck(processor)
        }
        jsonMapper {
            gsonBuilder.setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .registerTypeAdapterFactory(KotlinTypeAdapterFactory())
                .enableComplexMapKeySerialization()
                .addSkyHanniTypeAdapters()
                .addNautilusTypeAdapters()
        }
        throwOnFailure()
    }

    fun save() = managedConfig.saveToFile()

    val editor by lazy(managedConfig::getEditor)

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

    // TODO: create annotation for automatically adding type adapters
    fun GsonBuilder.addNautilusTypeAdapters(): GsonBuilder {
        return registerTypeAdapter(SemVersion.TYPE_ADAPTER)
            .registerTypeAdapter(TimePeriod.TYPE_ADAPTER)
            .registerTypeAdapter(FishingCategory.TYPE_ADAPTER)
            .registerTypeAdapter(FishingEvent.TYPE_ADAPTER)
            .registerTypeAdapterFactory(EmptyFieldAdapterFactory)
    }

    // TODO: find a way to get all factories in skyhanni's base gson builder dynamically
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
            .registerTypeAdapter(LegacyStringChromaColourTypeAdapter(true))

}
