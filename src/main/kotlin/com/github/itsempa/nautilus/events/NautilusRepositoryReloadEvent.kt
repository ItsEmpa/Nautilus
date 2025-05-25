package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import com.github.itsempa.nautilus.data.core.NautilusRepoManager
import com.github.itsempa.nautilus.utils.tryOrDefault
import com.github.itsempa.nautilus.utils.tryOrNull
import com.google.gson.Gson
import java.io.File
import java.lang.reflect.Type

class NautilusRepositoryReloadEvent(val repoLocation: File, val gson: Gson) : SkyHanniEvent() {
    inline fun <reified T : Any> getConstant(constant: String, type: Type? = null, gson: Gson = this.gson): T = try {
        NautilusRepoManager.setLastConstant(constant)
        if (!repoLocation.exists()) throw NautilusRepoManager.RepoError("Repo folder does not exist!")
        NautilusRepoManager.getConstant(repoLocation, constant, gson, T::class.java, type)
    } catch (e: Exception) {
        throw NautilusRepoManager.RepoError("Repo parsing error while trying to read constant '$constant'", e)
    }

    inline fun <reified T : Any> getConstantOrNull(
        constant: String,
        type: Type? = null,
        gson: Gson = this.gson,
    ): T? = tryOrNull { getConstant<T>(constant, type, gson) }

    inline fun <reified T : Any> getConstantOrDefault(
        constant: String,
        type: Type? = null,
        gson: Gson = this.gson,
        default: () -> T,
    ): T = tryOrDefault(default) { getConstant<T>(constant, type, gson) }
}
