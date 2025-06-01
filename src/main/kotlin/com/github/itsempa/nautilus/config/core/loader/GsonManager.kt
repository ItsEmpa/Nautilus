package com.github.itsempa.nautilus.config.core.loader

import at.hannibal2.skyhanni.deps.moulconfig.LegacyStringChromaColourTypeAdapter
import at.hannibal2.skyhanni.utils.json.BaseGsonBuilder
import at.hannibal2.skyhanni.utils.json.SkyHanniTypeAdapters
import com.github.itsempa.nautilus.data.categories.FishingCategory
import com.github.itsempa.nautilus.data.fishingevents.FishingEvent
import com.github.itsempa.nautilus.features.misc.update.SemVersion
import com.github.itsempa.nautilus.utils.TimePeriod
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter

object GsonManager {

    fun gsonBuilder(): GsonBuilder = BaseGsonBuilder.gson().addNautilusTypeAdapters()
    fun lenientGsonBuilder(): GsonBuilder = BaseGsonBuilder.lenientGson().addNautilusTypeAdapters()

    val gson: Gson = gsonBuilder().create()
    val lenientGson: Gson = lenientGsonBuilder().create()



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
    }

    // TODO: find a way to get all factories in skyhanni's base gson builder dynamically
    fun GsonBuilder.addSkyHanniTypeAdapters(): GsonBuilder =
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
