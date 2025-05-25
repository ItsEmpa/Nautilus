package com.github.itsempa.nautilus.features.chat

import at.hannibal2.skyhanni.api.event.HandleEvent
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.NautilusTimeUtils.customFormat
import me.owdding.ktmodules.Module

@Module
object SeaCreatureKillTimer {

    private val config get() = Nautilus.feature.chat.seaCreatureKillTimer

    // TODO: add to repo
    private val allowedMobs = setOf(
        "Thunder",
        "Lord Jawbus",
        "Ragnarok",
        "Wiki Tiki",
    )

    private fun isAllowed(name: String) = name in allowedMobs

    @HandleEvent
    fun onSeaCreatureDeath(event: SeaCreatureEvent.Death) {
        if (!config) return
        val seaCreature = event.seaCreature
        if (!isAllowed(event.name)) return
        val time = seaCreature.spawnTime.passedSince()
        if (event.seenDeath) {
            NautilusChat.chat("${seaCreature.displayName}§3 took §b${time.customFormat(showDeciseconds = true)}§3 to die.")
        } else {
            val minTime = seaCreature.lastUpdate.passedSince()
            val message = "${seaCreature.displayName}§3 took between " +
                "§b${minTime.customFormat(showDeciseconds = true)} §3and " +
                "§b${time.customFormat(showDeciseconds = true)}§3 to die."
            NautilusChat.chat(message)
        }
    }



}
