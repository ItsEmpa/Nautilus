package com.github.itsempa.nautilus.features.misc.update

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.hypixel.HypixelJoinEvent
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.core.NautilusStorage
import me.owdding.ktmodules.Module

@Module
object ChangelogManager {

    private val storage get() = NautilusStorage.storage

    @HandleEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        val lastVersion = storage.lastUsedVersion
        val currentVersion = Nautilus.SEM_VER
        val versionRange = lastVersion..currentVersion
        if (versionRange.isEmpty()) return
        storage.lastUsedVersion = currentVersion
        showChangelog(versionRange)
    }

    fun showChangelog(range: VersionRange) {
        // TODO: show changelog
    }

}
