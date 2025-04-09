package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.fishing.FishingApi
import at.hannibal2.skyhanni.features.fishing.SeaCreature
import at.hannibal2.skyhanni.features.fishing.SeaCreatureManager
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.exactBoundingBox
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.getLorenzVec
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusRenderUtils.drawBoundingBox
import com.github.itsempa.nautilus.utils.NautilusUtils.entityId
import com.github.itsempa.nautilus.utils.NautilusUtils.exactLocation
import com.github.itsempa.nautilus.utils.NautilusUtils.getLorenzVec
import com.github.itsempa.nautilus.utils.NautilusUtils.hasDied
import com.google.common.cache.RemovalCause
import java.awt.Color
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

// TODO: figure out why some mobs do not get removed from the cache, why some get added all at once,
//  and why pyroclastic worms sometimes get removed
@Suppress("UnstableApiUsage")
@Module
object SeaCreatureData {

    data class SeaCreatureData(
        var isOwn: Boolean,
        val seaCreature: SeaCreature,
        var entityId: Int,
        val spawnTime: SimpleTimeMark,
        var mob: Mob?,
        var lastKnownPos: LorenzVec?,
    ) {
        val isRare: Boolean get() = seaCreature.rare

        val despawnTime: SimpleTimeMark get() = spawnTime + DESPAWN_TIME

        fun isLoaded(): Boolean = mob != null

        fun getExactPosOrLast(renderWorld: SkyHanniRenderWorldEvent): LorenzVec? {
            val mob = mob ?: return lastKnownPos
            return if (mob.canBeSeen()) renderWorld.exactLocation(mob).also { lastKnownPos = it }
            else lastKnownPos
        }
    }

    private val DESPAWN_TIME = 6.minutes

    private val entityIdToData = TimeLimitedCache<Int, SeaCreatureData>(DESPAWN_TIME) { id, data, cause ->
        if (cause == RemovalCause.EXPIRED && data != null && id != null) {
            // TODO: add event for sea creature removal
        }
    }
    private val seaCreatures = mutableMapOf<Mob, SeaCreatureData>()

    private var lastNameFished: String? = null
    private var mobsToFind = 0
    private var lastSeaCreatureFished = SimpleTimeMark.farPast()

    private val recentMobs = TimeLimitedCache<Mob, SimpleTimeMark>(1.seconds) { mob, time, cause ->
        if (cause == RemovalCause.EXPIRED && mob != null && time != null) addMob(mob, time, isOwn = false)
    }

    private var lastBobberLocation: LorenzVec? = null

    private var babyMagmaSlugsToFind = 0
    private var lastMagmaSlugLocation: LorenzVec? = null
    private var lastMagmaSlugTime = SimpleTimeMark.farPast()

    private val recentBabyMagmaSlugs = TimeLimitedCache<Mob, SimpleTimeMark>(1.seconds) { mob, time, cause ->
        if (cause == RemovalCause.EXPIRED && mob != null && time != null) addMob(mob, time, isOwn = false, isBabySlug = true)
    }

    @HandleEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        val mob = event.mob
        val data = entityIdToData[mob.entityId]
        if (data != null) {
            seaCreatures[mob] = data
            data.mob = mob
            return
        }

        if (mob.name == "Baby Magma Slug") {
            recentBabyMagmaSlugs[mob] = SimpleTimeMark.now()
            DelayedRun.runNextTick {
                handleBabySlugs()
            }
            return
        }
        if (mob.name !in SeaCreatureManager.allFishingMobs) return
        recentMobs[mob] = SimpleTimeMark.now()
        handleOwnMob()
    }

    @HandleEvent
    fun onMobDespawn(event: MobEvent.DeSpawn.SkyblockMob) {
        val mob = event.mob
        recentBabyMagmaSlugs.remove(mob)
        recentMobs.remove(mob)
        val data = seaCreatures[mob] ?: return
        seaCreatures.remove(mob)
        val oldId = data.entityId
        val newId = mob.entityId
        if (mob.hasDied) {
            entityIdToData.remove(newId)
            if (data.isOwn) {
                if (mob.name == "Magma Slug") {
                    lastMagmaSlugLocation = mob.getLorenzVec()
                    babyMagmaSlugsToFind += 3
                    lastMagmaSlugTime = SimpleTimeMark.now()
                    handleBabySlugs()
                }
                // TODO: create event for own sea creature dying
            }
            return
        } else if (oldId != newId) { // we update the entity id in case the baseEntity has changed at some point
            entityIdToData.remove(oldId)
            entityIdToData[newId] = data
            data.entityId = newId
        }
        data.mob = null
    }

    @HandleEvent
    fun onSeaCreatureFish(event: SeaCreatureFishEvent) {
        lastSeaCreatureFished = SimpleTimeMark.now()
        lastNameFished = event.seaCreature.name
        mobsToFind = if (event.doubleHook) 2 else 1
        handleOwnMob()
    }

    private fun addMob(
        mob: Mob,
        time: SimpleTimeMark = SimpleTimeMark.now(),
        isOwn: Boolean = false,
        isBabySlug: Boolean = false,
    ) {
        val seaCreature = if (isBabySlug) BABY_MAGMA_SLUG else SeaCreatureManager.allFishingMobs[mob.name] ?: return
        val pos = if (isOwn || mob.canBeSeen()) mob.getLorenzVec() else null
        val data = SeaCreatureData(isOwn, seaCreature, mob.entityId, time, mob, pos)
        // TODO: add event for sea creature spawn
        seaCreatures[mob] = data
        entityIdToData[mob.entityId] = data
    }

    private fun handleOwnMob() {
        if (lastSeaCreatureFished.passedSince() > 1.seconds) return
        val name = lastNameFished ?: return
        // TODO: create a sortedByFiltered function that removes elements when the comparator returns null
        val mobs = recentMobs.asSequence().filter { (mob, _) -> mob.name == name }.map {
            it to it.key.baseEntity.distanceTo(lastBobberLocation!!)
        }.filter { it.second <= 3 }
            .sortedBy { it.second }
            .take(mobsToFind).toList()

        if (mobs.isEmpty()) return
        mobsToFind -= mobs.size
        for ((entry, _) in mobs) {
            val (mob, time) = entry
            addMob(mob, time, isOwn = true)
            recentMobs.remove(mob)
        }
        if (mobsToFind == 0) lastNameFished = null
    }

    private fun handleBabySlugs() {
        if (lastMagmaSlugTime.passedSince() > 1.seconds) return
        if (babyMagmaSlugsToFind == 0) return
        val location = lastMagmaSlugLocation ?: return
        val slugs = recentBabyMagmaSlugs.asSequence().map {
            it to it.key.baseEntity.distanceTo(location)
        }.filter { it.second <= 2 }
            .sortedBy { it.second }
            .take(babyMagmaSlugsToFind).toList()

        if (slugs.isEmpty()) return
        babyMagmaSlugsToFind -= slugs.size
        for ((entry, _) in slugs) {
            val (mob, time) = entry
            addMob(mob, time, isOwn = true, isBabySlug = true)
            recentBabyMagmaSlugs.remove(mob)
        }
        if (babyMagmaSlugsToFind == 0) {
            lastMagmaSlugLocation = null
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        for ((mob, data) in seaCreatures) {
            val color = if (data.isOwn) Color.GREEN else Color.BLUE
            event.drawBoundingBox(event.exactBoundingBox(mob.baseEntity), color, wireframe = true)
        }
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        recentMobs.entries // TODO: find a better way to force the cleanup of the cache
        recentBabyMagmaSlugs.entries
        if (babyMagmaSlugsToFind != 0 && lastMagmaSlugTime.passedSince() > 2.seconds) babyMagmaSlugsToFind = 0
        val bobber = FishingApi.bobber ?: return
        lastBobberLocation = bobber.getLorenzVec()
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        entityIdToData.clear()
        seaCreatures.clear() // TODO: add event for sea creature removal
        recentMobs.clear()
        recentBabyMagmaSlugs.clear()
        lastBobberLocation = null
        lastMagmaSlugLocation = null
        babyMagmaSlugsToFind = 0
        lastMagmaSlugTime = SimpleTimeMark.farPast()
        lastSeaCreatureFished = SimpleTimeMark.farPast()
        lastNameFished = null
        mobsToFind = 0
    }

    val BABY_MAGMA_SLUG = SeaCreature(
        "Baby Magma Slug",
        fishingExperience = 730,
        chatColor = "§c",
        rare = false,
        rarity = LorenzRarity.RARE,
    )

    @HandleEvent
    fun onDebugCollect(event: DebugDataCollectEvent) {
        event.title("Nautilus Sea Creatures")
        event.addIrrelevant {
            add("entityIdToData: ${entityIdToData.entries}")
            add("seaCreatures: $seaCreatures")
            add("recentMobs: ${recentMobs.entries}")
            add("lastNameFished: $lastNameFished")
            add("mobsToFind: $mobsToFind")
            add("lastSeaCreatureFished: $lastSeaCreatureFished")
            add("lastBobberLocation: $lastBobberLocation")
            add("babyMagmaSlugsToFind: $babyMagmaSlugsToFind")
            add("lastMagmaSlugLocation: $lastMagmaSlugLocation")
            add("lastMagmaSlugTime: $lastMagmaSlugTime")
            add("recentBabyMagmaSlugs: ${recentBabyMagmaSlugs.entries}")
        }
    }


}
