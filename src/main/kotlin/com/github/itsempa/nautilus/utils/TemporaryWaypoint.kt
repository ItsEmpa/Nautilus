package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import me.owdding.ktmodules.Module
import kotlin.time.Duration

class TemporaryWaypoint(
    private val maxTime: Duration,
    private val minDistance: Double,
) {
    private var position: LorenzVec? = null
    private var time = SimpleTimeMark.farPast()

    fun setPos(pos: LorenzVec) {
        position = pos
        time = SimpleTimeMark.now()
        displayedWaypoints.add(this)
    }

    fun getPos(): LorenzVec? = position

    fun reset() {
        resetData()
        displayedWaypoints.remove(this)
    }

    private fun resetData() {
        position = null
    }

    private fun shouldRemove(): Boolean {
        if (time.passedSince() > maxTime) return true
        val pos = position ?: return false
        return pos.distanceToPlayer() < minDistance
    }

    @Module
    companion object {
        private val displayedWaypoints = mutableListOf<TemporaryWaypoint>()

        @HandleEvent
        fun onWorldChange() = displayedWaypoints.clearAnd(TemporaryWaypoint::resetData)

        @HandleEvent
        fun onSecondPassed() {
            if (displayedWaypoints.isEmpty()) return
            displayedWaypoints.removeIf { wp ->
                val shouldRemove = wp.shouldRemove()
                if (shouldRemove) wp.resetData()
                shouldRemove
            }
        }
    }
}
