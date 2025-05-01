package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.ClipboardUtils
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.cleanName
import at.hannibal2.skyhanni.utils.EnumUtils.toFormattedName
import at.hannibal2.skyhanni.utils.LocationUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.MobUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.getLorenzVec
import com.github.itsempa.nautilus.events.FishCatchEvent
import com.github.itsempa.nautilus.events.HotspotEvent
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.NautilusUtils.clearAnd
import com.github.itsempa.nautilus.utils.NautilusUtils.expandToInclude
import com.github.itsempa.nautilus.utils.NautilusUtils.getCenter
import net.minecraft.block.BlockLiquid
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EnumParticleTypes
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Module
object HotspotApi {

    private const val MAX_RADIUS = 10.0
    const val HOTSPOT_NAMETAG = "§d§lHOTSPOT"

    enum class HotspotBuff(color: LorenzColor, amount: Int, icon: Char, displayName: String? = null) {
        TREASURE_CHANCE(LorenzColor.GOLD, 1, '⛃'),
        SEA_CREATURE_CHANCE(LorenzColor.DARK_AQUA, 5, 'α'),
        DOUBLE_HOOK_CHANCE(LorenzColor.DARK_BLUE, 2, '⚓'),
        FISHING_SPEED(LorenzColor.AQUA, 15, '☂'),
        TROPHY_FISH_CHANCE(LorenzColor.GOLD, 5, '♔'), // TODO: confirm this icon
        UNKNOWN(LorenzColor.BLACK, 0, '?')
        ;

        val statName: String = displayName ?: toFormattedName()
        val string: String = "+$amount$icon $statName"
        val displayName: String = color.getChatColor() + string
        override fun toString(): String = displayName

        companion object {
            val default get() = entries.toMutableList().apply { remove(UNKNOWN) }
            fun getByStatName(name: String): HotspotBuff = entries.find { it.statName.equals(name, true) } ?: UNKNOWN
            fun getByName(name: String): HotspotBuff? = entries.find { it.string == name }
            fun getByNameOrUnknown(name: String): HotspotBuff = getByName(name) ?: UNKNOWN
        }
    }

    class Hotspot(firstParticle: LorenzVec) {
        var lastUpdate: SimpleTimeMark
            private set
        val startTime: SimpleTimeMark

        @Deprecated("Intended only for internal use")
        internal var aabb: AxisAlignedBB = firstParticle.axisAlignedTo(firstParticle)
            private set
        var center: LorenzVec = firstParticle
            private set
        var radius: Double = 0.0
            private set
        var buff: HotspotBuff? = null
            private set
        var hasBeenSeen: Boolean = false
            private set

        init {
            val now = SimpleTimeMark.now()
            lastUpdate = now
            startTime = now
            updateBuff()
            checkSeen()
        }

        fun distance(pos: LorenzVec): Double = center.distanceIgnoreY(pos)
        fun isInside(pos: LorenzVec): Boolean = distance(pos) <= radius
        fun isInside(entity: Entity): Boolean = isInside(entity.getLorenzVec())

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

        fun tick() {
            updateBuff()
            checkSeen()
        }

        fun checkSeen() {
            if (hasBeenSeen) return
            if (!center.up(2).canBeSeen()) return // TODO: use proper canBeSeen detection
            hasBeenSeen = true
            HotspotEvent.Seen(this).post()
        }

        fun updateBuff() {
            if (buff != null) return
            val possibleEntities = EntityUtils.getEntities<EntityArmorStand>().filter { isInside(it) }
            val baseEntity = possibleEntities.find { it.name == HOTSPOT_NAMETAG } ?: return
            val next = MobUtils.getNextEntity(baseEntity, 1) ?: return
            buff = HotspotBuff.getByNameOrUnknown(next.cleanName())
            HotspotEvent.BuffFound(this).post()
        }

        override fun toString(): String {
            val buffString = buff?.toString() ?: "Unknown"
            return "Hotspot(center=$center, radius=$radius, buff=$buffString, lastUpdate=$lastUpdate)"
        }
    }

    private val _hotspots = mutableListOf<Hotspot>()
    val hotspots: List<Hotspot> get() = _hotspots

    // TODO: make better detection, this is ass
    fun isHotspotFishing(): Boolean = lastNearFishedHotspotTime.passedSince() < 4.minutes && lastHotspotFish.passedSince() < 2.minutes

    private var lastNearFishedHotspotTime = SimpleTimeMark.farPast()

    fun isInHotspot(pos: LorenzVec) = _hotspots.any { it.isInside(pos) }

    var lastHotspotFish: SimpleTimeMark = SimpleTimeMark.farPast()
        private set

    var lastHotspotPos: LorenzVec? = null
        private set

    private fun ReceiveParticleEvent.isHotspotParticle(): Boolean {
        return when (type) {
            EnumParticleTypes.SMOKE_NORMAL -> {
                speed == 0f && (count == 5 || count == 2)
            }
            EnumParticleTypes.REDSTONE -> {
                count == 0 && speed == 1f
            }
            else -> false
        }
    }

    @HandleEvent(onlyOnIslands = [IslandType.HUB, IslandType.SPIDER_DEN, IslandType.BACKWATER_BAYOU, IslandType.CRIMSON_ISLE])
    fun onParticleReceive(event: ReceiveParticleEvent) {
        if (!event.isHotspotParticle()) return
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

    @HandleEvent(onlyOnIslands = [IslandType.HUB, IslandType.SPIDER_DEN, IslandType.BACKWATER_BAYOU, IslandType.CRIMSON_ISLE])
    fun onSecondPassed(event: SecondPassedEvent) {
        _hotspots.removeIf {
            val isOld = it.lastUpdate.passedSince() > 2.seconds
            if (isOld) HotspotEvent.Removed(it).post()
            isOld
        }
        if (isNearFishedHotspot()) lastNearFishedHotspotTime = SimpleTimeMark.now()
    }

    private fun isNearFishedHotspot(): Boolean {
        val lastPos = lastHotspotPos ?: return false
        if (!isInHotspot(lastPos)) return false
        return lastPos.distanceToPlayer() < 30
    }

    @HandleEvent(onlyOnIslands = [IslandType.HUB, IslandType.SPIDER_DEN, IslandType.BACKWATER_BAYOU, IslandType.CRIMSON_ISLE])
    fun onTick() = _hotspots.forEach(Hotspot::tick)

    @HandleEvent(onlyOnIslands = [IslandType.HUB, IslandType.SPIDER_DEN, IslandType.BACKWATER_BAYOU, IslandType.CRIMSON_ISLE])
    fun onCatch(event: FishCatchEvent) {
        val pos = event.bobberPos
        if (!isInHotspot(pos)) return
        lastHotspotFish = SimpleTimeMark.now()
        lastHotspotPos = pos
    }

    @HandleEvent
    fun onCommandRegistration(event: NautilusCommandRegistrationEvent) {
        event.register("ntdebughotspot") {
            this.description = "Copies Hotspot Debug Data to clipboard."
            this.category = CommandCategory.DEVELOPER_DEBUG
            callback {
                NautilusChat.chat("Copied Hotspot data to clipboard.")
                ClipboardUtils.copyToClipboard(_hotspots.toString())
            }
        }
    }

    @HandleEvent
    fun onWorldChange() {
        lastHotspotPos = null
        _hotspots.clearAnd { hotspot ->
            HotspotEvent.Removed(hotspot).post()
        }
        lastNearFishedHotspotTime = SimpleTimeMark.farPast()
    }

}
