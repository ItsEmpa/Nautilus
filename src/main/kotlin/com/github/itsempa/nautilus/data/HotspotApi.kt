package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.github.itsempa.nautilus.events.FishCatchEvent
import com.github.itsempa.nautilus.events.HotspotEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusUtils.clearAnd
import com.github.itsempa.nautilus.utils.NautilusUtils.expandToInclude
import com.github.itsempa.nautilus.utils.NautilusUtils.getCenter
import net.minecraft.block.BlockLiquid
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EnumParticleTypes
import kotlin.time.Duration.Companion.seconds

@Module
object HotspotApi {

    private const val MAX_RADIUS = 10.0
    const val HOTSPOT_NAMETAG = "§d§lHOTSPOT"

    class Hotspot(firstParticle: LorenzVec) {
        var lastUpdate: SimpleTimeMark = SimpleTimeMark.now()
            private set

        @Deprecated("Intended only for internal use")
        internal var aabb: AxisAlignedBB = firstParticle.axisAlignedTo(firstParticle)
            private set
        var center: LorenzVec = firstParticle
            private set
        var radius: Double = 0.0
            private set

        fun distance(pos: LorenzVec): Double = center.distanceIgnoreY(pos)
        fun isInside(pos: LorenzVec): Boolean = pos.distance(center) <= radius

        fun updateTime() {
            lastUpdate = SimpleTimeMark.now()
        }

        @Suppress("DEPRECATION")
        fun addParticle(pos: LorenzVec) {
            updateTime()
            aabb = aabb.expandToInclude(pos)
            center = aabb.getCenter()
            radius = distance(pos)
        }
    }

    private val _hotspots = mutableListOf<Hotspot>()
    val hotspots: List<Hotspot> get() = _hotspots

    fun isInHotspot(pos: LorenzVec) = _hotspots.any { it.isInside(pos) }

    var lastHotspotFish: SimpleTimeMark = SimpleTimeMark.farPast()
        private set

    var lastHotspotPos: LorenzVec? = null
        private set

    @HandleEvent
    fun onParticleReceive(event: ReceiveParticleEvent) {
        if (event.type != EnumParticleTypes.SMOKE_NORMAL || event.speed != 0f || event.count != 5) return
        val pos = event.location
        if (pos.getBlockAt() !is BlockLiquid && pos.down().getBlockAt() !is BlockLiquid) return
        val hotspot = _hotspots.find {
            it.isInside(pos) || it.distance(pos) < MAX_RADIUS
        }
        if (hotspot == null) {
            val newHotspot = Hotspot(pos)
            _hotspots.add(newHotspot)
            HotspotEvent.Detected(newHotspot).post()
            return
        }
        hotspot.addParticle(pos)
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        _hotspots.removeIf {
            val isOld = it.lastUpdate.passedSince() > 2.seconds
            if (isOld) HotspotEvent.Removed(it).post()
            isOld
        }
    }

    @HandleEvent
    fun onCatch(event: FishCatchEvent) {
        val pos = event.bobberPos
        if (!isInHotspot(pos)) return
        lastHotspotFish = SimpleTimeMark.now()
        lastHotspotPos = pos
    }

    @HandleEvent
    fun onWorldChange() {
        lastHotspotPos = null
        _hotspots.clearAnd { hotspot ->
            HotspotEvent.Removed(hotspot).post()
        }
    }

}
