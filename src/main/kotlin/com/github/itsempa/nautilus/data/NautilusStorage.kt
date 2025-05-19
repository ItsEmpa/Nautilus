package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.config.storage.ProfileStorage
import com.github.itsempa.nautilus.config.storage.Storage
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat

@Module
object NautilusStorage {

    val storage: Storage get() = Nautilus.feature.storage

    var profile: ProfileStorage = ProfileStorage()
        private set

    var profileName: String? = null
        private set

    @HandleEvent(priority = HandleEvent.HIGHEST)
    fun onProfileJoin(event: ProfileJoinEvent) {
        NautilusChat.debug("Joined profile ${event.name}")
        profile = storage.profileStorage.getOrPut(event.name, ::ProfileStorage)
        profileName = event.name
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) {
        NautilusChat.debug("Disconnected from server")
        profile = ProfileStorage()
        profileName = null
    }

    @HandleEvent
    fun onDebug(event: NautilusDebugEvent) {
        event.title("Nautilus Storage")
        event.addIrrelevant(
            "profileName" to profileName,
            "profileStorage" to profile,
        )
    }

}
