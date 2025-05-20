package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.features.fishing.FishingApi
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.getLorenzVec
import com.github.itsempa.nautilus.events.FishCatchEvent
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.ResettingValue
import com.github.itsempa.nautilus.utils.helpers.McPlayer
import kotlin.time.Duration.Companion.seconds

@Module
object FishingReelApi {

    var lastBobberLocation: LorenzVec? = null
        private set

    private val delay = 2.seconds

    private var foundPlingSound: Boolean by ResettingValue(delay, false)
    private var foundCatchSound: Boolean by ResettingValue(delay, false)

    private var lastYaw: Float = 0f
    private var lastPitch: Float = 0f

    var catchesSinceMove: Int = 0
        private set

    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.HIGHEST)
    fun onTick() {
        if (catchesSinceMove > 0) {
            val newYaw = McPlayer.yaw
            val newPitch = McPlayer.pitch
            if (newYaw != lastYaw || newPitch != lastPitch) {
                lastYaw = newYaw
                lastPitch = newPitch
                resetCatches()
            }
        }
        val bobber = FishingApi.bobber ?: return
        lastBobberLocation = bobber.getLorenzVec()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onPlaySound(event: PlaySoundEvent) {
        when (event.soundName) {
            "game.player.swim.splash" -> {
                if (event.volume != 0.25f) return
                val lastBobber = lastBobberLocation ?: return
                if (event.location.distance(lastBobber) > 2) return
                foundCatchSound = true
                handleBobber()
            }

            "note.pling" -> {
                if (event.pitch != 1f || event.volume != 1f) return
                if (event.distanceToPlayer > 2) return
                foundPlingSound = true
                handleBobber()
            }
        }
    }

    private fun handleBobber() {
        if (!foundPlingSound || !foundCatchSound) return
        ++catchesSinceMove
        FishCatchEvent(catchesSinceMove, lastBobberLocation!!).post()
        foundPlingSound = false
        foundCatchSound = false
    }

    private fun resetCatches() {
        catchesSinceMove = 0
    }

    @HandleEvent
    fun onWorldChange() {
        lastBobberLocation = null
        foundPlingSound = false
        foundCatchSound = false
        resetCatches()
    }

    @HandleEvent
    fun onDebug(event: NautilusDebugEvent) {
        event.title("Fishing Reel Api") {
            addIrrelevant(
                "lastBobberLocation" to lastBobberLocation,
                "foundPlingSound" to foundPlingSound,
                "foundCatchSound" to foundCatchSound,
                "catchesSinceMove" to catchesSinceMove,
            )
        }
    }
}
