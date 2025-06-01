package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.entity.EntityCustomNameUpdateEvent
import at.hannibal2.skyhanni.events.entity.EntityLeaveWorldEvent
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockStateAt
import at.hannibal2.skyhanni.utils.EnumUtils.toFormattedName
import at.hannibal2.skyhanni.utils.LocationUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.getLorenzVec
import com.github.itsempa.nautilus.events.FishCatchEvent
import com.github.itsempa.nautilus.events.HotspotEvent
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.features.render.HotspotHighlight
import com.github.itsempa.nautilus.utils.NautilusUtils.roundToHalf
import com.github.itsempa.nautilus.utils.clearAnd
import com.github.itsempa.nautilus.utils.helpers.McPlayer
import me.owdding.ktmodules.Module
import net.minecraft.block.BlockLiquid
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.EnumParticleTypes
import java.awt.Color
import java.util.LinkedList
import kotlin.math.abs
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Module
object HotspotApi {

    private const val MAX_RADIUS = 10.0
    private const val HOTSPOT_NAMETAG = "§d§lHOTSPOT"

    enum class HotspotBuff(color: LorenzColor, amount: Int, icon: Char, displayName: String? = null) {
        TREASURE_CHANCE(LorenzColor.GOLD, 1, '⛃'),
        SEA_CREATURE_CHANCE(LorenzColor.DARK_AQUA, 5, 'α'),
        DOUBLE_HOOK_CHANCE(LorenzColor.DARK_BLUE, 2, '⚓'),
        FISHING_SPEED(LorenzColor.AQUA, 15, '☂'),
        TROPHY_FISH_CHANCE(LorenzColor.GOLD, 5, '♔'),
        UNKNOWN(LorenzColor.GRAY, 0, '?')
        ;

        val statName: String = displayName ?: toFormattedName()
        val string: String = "+$amount$icon $statName"
        val color: Color = color.toColor()
        val displayName: String = color.getChatColor() + string
        override fun toString(): String = displayName

        companion object {
            val default get() = entries.toMutableList().apply { remove(UNKNOWN) }
            fun getByStatName(name: String): HotspotBuff = entries.find { it.statName.equals(name, true) } ?: UNKNOWN
            fun getByName(name: String): HotspotBuff? = entries.find { it.string == name }
            fun getByNameOrUnknown(name: String): HotspotBuff = getByName(name) ?: UNKNOWN
        }
    }

    data class Hotspot(val center: LorenzVec, private val entityId: Int) {
        val startTime: SimpleTimeMark = SimpleTimeMark.now()
        var lastUpdate: SimpleTimeMark = SimpleTimeMark.now()
            private set
        var radius: Double = Double.NaN
            private set

        var buff: HotspotBuff? = null
            private set
        var hasBeenSeen: Boolean = false
            private set

        private val radiusCount: MutableMap<Double, Int> = mutableMapOf()

        init {
            tick()
        }

        private var isConfirmed = false
        private var hasSentBuff = false

        fun distance(pos: LorenzVec): Double = center.distanceIgnoreY(pos)
        fun isInside(pos: LorenzVec): Boolean = distance(pos) <= radius

        fun isDetected() = isConfirmed

        /** Returns true if it "used" the particle. */
        fun tryAddParticle(pos: LorenzVec): Boolean {
            val distance = distance(pos)
            if (distance > MAX_RADIUS) return false

            val newRadius = distance.roundToHalf()

            if (!isConfirmed) {
                // This is done to prevent particles from other sources from flickering the radius.
                val newCount = radiusCount.addOrPut(newRadius, 1)
                radius = newRadius
                if (newCount > 15) {
                    isConfirmed = true

                    HotspotEvent.Detected(this).post()
                    postBuff()

                    radiusCount.clear()
                }
            } else if (abs(newRadius - radius) > 1.0) return false

            lastUpdate = SimpleTimeMark.now()

            return true
        }

        fun tick() = checkSeen()

        private fun checkSeen() {
            if (hasBeenSeen) return
            if (!center.canBeSeen()) return // TODO: fix this
            //if (!FrustumUtils.isVisible(aabb)) return

            hasBeenSeen = true
            HotspotEvent.Seen(this).post()
        }

        private fun postBuff() {
            if (!isConfirmed || hasSentBuff) return
            if (buff == null) return
            hasSentBuff = true
            HotspotEvent.BuffFound(this).post()
        }

        fun updateBuff(name: String) {
            if (buff != null) return
            buff = HotspotBuff.getByName(name) ?: return
            postBuff()
        }

        override fun toString(): String {
            val buffString = buff?.toString() ?: "Unknown"
            return "Hotspot(" +
                "center=$center, " +
                "radius=$radius, " +
                "buff=$buffString, " +
                "hasBeenSeen=$hasBeenSeen, " +
                "entityId=$entityId" +
                "lastUpdate=${lastUpdate.passedSince()}, " +
                "startTime=${startTime.passedSince()}" +
                ")"
        }
    }

    private val _hotspots = mutableMapOf<Int, Hotspot>()
    val hotspots: Sequence<Hotspot> get() = _hotspots.values.asSequence().filter { it.isDetected() }
    var currentHotspot: Hotspot? = null
        private set

    private val despawnQueue = LinkedList<Pair<SimpleTimeMark, Hotspot>>()

    // TODO: make better detection, this is ass
    //  this is slightly better now, but i still dont really like it a lot
    fun isHotspotFishing(): Boolean {
        val hotspot = currentHotspot
        if (hotspot != null) return true
        return lastNearFishedHotspotTime.passedSince() < 1.minutes
    }

    private var lastNearFishedHotspotTime = SimpleTimeMark.farPast()

    fun isInHotspot(pos: LorenzVec) = hotspots.any { it.isInside(pos) }

    var lastHotspotFish: SimpleTimeMark = SimpleTimeMark.farPast()
        private set

    var lastHotspotPos: LorenzVec? = null
        private set

    private fun ReceiveParticleEvent.isHotspotParticle(): Boolean {
        return if (IslandType.CRIMSON_ISLE.isCurrent()) {
            type == EnumParticleTypes.SMOKE_NORMAL && speed == 0f && (count == 5 || count == 2)
        } else {
            type == EnumParticleTypes.REDSTONE && speed == 1f && count == 0
        }
    }

    private fun EntityCustomNameUpdateEvent<EntityArmorStand>.handleHotspot(): Boolean {
        if (newName != HOTSPOT_NAMETAG) return false
        val entity = entity

        val realPos = entity.getLorenzVec()
        val pos = findLava(realPos) ?: return true
        val id = entity.entityId
        if (id in _hotspots) return true

        val hotspot = Hotspot(pos, id)
        _hotspots[id] = hotspot
        return true
    }

    private fun EntityCustomNameUpdateEvent<EntityArmorStand>.handleBuff(): Boolean {
        val entity = entity
        val hotspot = _hotspots.values.find { it.distance(entity.getLorenzVec()) < 0.1 } ?: return false
        val name = newName?.removeColor() ?: return false
        hotspot.updateBuff(name)
        return true
    }

    // TODO: do without HOTSPOT armorstand
    // This seems to break if there is an island check?
    @HandleEvent
    fun onEntityCustomNameUpdate(event: EntityCustomNameUpdateEvent<EntityArmorStand>) {
        event.handleHotspot() || event.handleBuff()
    }

    private fun findLava(pos: LorenzVec): LorenzVec? {
        val snapped = pos.copy(y = pos.y.toInt().toDouble())
        for (i in 3 downTo -3) {
            val newPos = snapped.up(i)
            val blockstate = newPos.getBlockStateAt()
            val block = blockstate.block
            if (block !is BlockLiquid) continue
            val air = BlockLiquid.getLiquidHeightPercent(block.getMetaFromState(blockstate))
            return newPos.up().down(air).up(0.01)
        }
        return null
    }

    @HandleEvent(onlyOnIslands = [IslandType.HUB, IslandType.SPIDER_DEN, IslandType.BACKWATER_BAYOU, IslandType.CRIMSON_ISLE])
    fun onEntityLeave(event: EntityLeaveWorldEvent<EntityArmorStand>) {
        val removed = _hotspots.remove(event.entity.entityId) ?: return
        despawnQueue.addLast(Pair(SimpleTimeMark.now(), removed))
        HotspotEvent.Removed(removed).post()
    }

    @HandleEvent(
        onlyOnIslands = [IslandType.HUB, IslandType.SPIDER_DEN, IslandType.BACKWATER_BAYOU, IslandType.CRIMSON_ISLE],
        receiveCancelled = true,
    )
    fun onParticleReceive(event: ReceiveParticleEvent) {
        if (!event.isHotspotParticle()) return
        val pos = event.location
        val anyAdded = _hotspots.values.any { it.tryAddParticle(pos) }
        if (anyAdded && HotspotHighlight.shouldHideHotspotParticles()) event.cancel()
    }

    @HandleEvent(onlyOnIslands = [IslandType.HUB, IslandType.SPIDER_DEN, IslandType.BACKWATER_BAYOU, IslandType.CRIMSON_ISLE])
    fun onSecondPassed() {
        if (isNearFishedHotspot()) lastNearFishedHotspotTime = SimpleTimeMark.now()
    }

    private fun isNearFishedHotspot(): Boolean {
        val hotspot = currentHotspot ?: return false
        return hotspot.center.distanceToPlayer() < 30
    }

    @HandleEvent(onlyOnIslands = [IslandType.HUB, IslandType.SPIDER_DEN, IslandType.BACKWATER_BAYOU, IslandType.CRIMSON_ISLE])
    fun onTick() {
        _hotspots.values.forEach(Hotspot::tick)
        pollDespawnQueue()
    }

    private fun pollDespawnQueue() {
        val playerPos = McPlayer.pos
        while (true) {
            val (time, hotspot) = despawnQueue.peek() ?: return
            if (time.passedSince() < 1.seconds) return
            despawnQueue.poll()
            if (hotspot.distance(playerPos) < 30) HotspotEvent.Ended(hotspot).post()
        }
    }

    @HandleEvent(onlyOnIslands = [IslandType.HUB, IslandType.SPIDER_DEN, IslandType.BACKWATER_BAYOU, IslandType.CRIMSON_ISLE])
    fun onCatch(event: FishCatchEvent) {
        val pos = event.bobberPos
        val hotspot = hotspots.find { it.isInside(pos) } ?: return
        if (currentHotspot != hotspot) {
            currentHotspot = hotspot
            HotspotEvent.StartFishing(hotspot).post()
        }
        lastNearFishedHotspotTime = SimpleTimeMark.now()
        lastHotspotFish = SimpleTimeMark.now()
        lastHotspotPos = pos
    }

    @HandleEvent
    fun onDebug(event: NautilusDebugEvent) {
        event.title("HotspotApi") {
            addIrrelevant(
                "hotspots" to _hotspots,
                "isHotspotFishing" to isHotspotFishing(),
                "lastNearFishedHotspotTime" to lastNearFishedHotspotTime,
                "lastHotspotFish" to lastHotspotFish,
                "lastHotspotPos" to lastHotspotPos,
            )
        }
    }

    @HandleEvent
    fun onWorldChange() {
        currentHotspot = null
        lastHotspotPos = null
        _hotspots.clearAnd { (_, hotspot) ->
            HotspotEvent.Removed(hotspot).post()
        }
        despawnQueue.clear()
        lastNearFishedHotspotTime = SimpleTimeMark.farPast()
    }

}
