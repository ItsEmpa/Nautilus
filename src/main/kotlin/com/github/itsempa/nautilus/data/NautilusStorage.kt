package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.config.storage.ProfileStorage
import com.github.itsempa.nautilus.config.storage.Storage
import com.github.itsempa.nautilus.modules.Module

@Module
object NautilusStorage {

    val storage: Storage get() = Nautilus.feature.storage

    var profile: ProfileStorage = ProfileStorage()
        private set

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        profile = storage.profileStorage.getOrPut(event.name, ::ProfileStorage)
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) {
        profile = ProfileStorage()
    }

}
