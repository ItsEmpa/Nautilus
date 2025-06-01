package com.github.itsempa.nautilus.config.core.loader

import at.hannibal2.skyhanni.deps.moulconfig.managed.DataMapper
import at.hannibal2.skyhanni.utils.json.fromJson
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.config.Features
import com.github.itsempa.nautilus.utils.helpers.McClient
import com.github.itsempa.nautilus.utils.tryOrDefault
import com.google.gson.JsonObject

object ConfigMapper : DataMapper<Features> {
    private val gson = GsonManager.lenientGson

    override fun serialize(value: Features): String = gson.toJson(value)

    override fun createDefault(): Features = Features()

    override fun deserialize(string: String): Features {
        return tryOrDefault(::createDefault) {
            val jsonObject = gson.fromJson<JsonObject>(string)
            val newJson = NautilusConfigMigrator.fixConfig(jsonObject)

            val run = { gson.fromJson<Features>(newJson) }
            if (PlatformUtils.isDevEnvironment) {
                try {
                    return run()
                } catch (e: Throwable) {
                    Nautilus.consoleLog(e.stackTraceToString())
                    McClient.shutdown("Nautilus Config is corrupt inside development environment.")
                }
            } else run()
            return gson.fromJson<Features>(string)
        }
    }
}
