package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import com.github.itsempa.nautilus.data.NautilusRepoManager
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
}
