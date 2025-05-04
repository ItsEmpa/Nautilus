package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.fishing.FishingApi
import at.hannibal2.skyhanni.features.fishing.SeaCreature
import at.hannibal2.skyhanni.features.fishing.SeaCreatureManager
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.getLorenzVec
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.entityId
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.getLorenzVec
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.hasDied
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.spawnTime
import com.github.itsempa.nautilus.utils.helpers.McPlayer
import com.github.itsempa.nautilus.utils.removeIf
import com.google.common.cache.RemovalCause
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

// TODO: maybe replace "handleMobs" functions with code in their event for faster detection?
@Suppress("UnstableApiUsage")
@Module
object SeaCreatureDetectionApi {

    val DESPAWN_TIME = 6.minutes

    private val entityIdToData = TimeLimitedCache<Int, SeaCreatureData>(DESPAWN_TIME) { id, data, cause ->
        if (cause == RemovalCause.EXPIRED && data != null && id != null) data.forceRemove()
    }

    fun getSeaCreatures(): Collection<SeaCreatureData> = entityIdToData.values
    private val seaCreatures = mutableMapOf<Mob, SeaCreatureData>()

    val Mob.seaCreature: SeaCreatureData? get() = seaCreatures[this]

    private var lastNameFished: String? = null
    private var mobsToFind = 0
    private var lastSeaCreatureFished = SimpleTimeMark.farPast()

    private val recentMobs = mutableMapOf<Mob, SimpleTimeMark>()

    private var lastBobberLocation: LorenzVec? = null

    private var babyMagmaSlugsToFind = 0
    private var lastMagmaSlugLocation: LorenzVec? = null
    private var lastMagmaSlugTime = SimpleTimeMark.farPast()

    private val recentBabyMagmaSlugs = mutableMapOf<Mob, SimpleTimeMark>()

    @HandleEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        val mob = event.mob
        val data = entityIdToData[mob.entityId]
        if (data != null) {
            seaCreatures[mob] = data
            data.mob = mob
            SeaCreatureEvent.ReDetect(data).post()
            return
        }

        if (mob.name == "Baby Magma Slug") {
            recentBabyMagmaSlugs[mob] = SimpleTimeMark.now()
            // TODO: test if baby magma slugs work without delayed run
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
        data.despawn()
        if (mob.hasDied) {
            entityIdToData.remove(oldId)
            data.forceRemove()
            if (data.isOwn) {
                if (mob.name == "Magma Slug") {
                    lastMagmaSlugLocation = mob.getLorenzVec()
                    babyMagmaSlugsToFind += 3
                    lastMagmaSlugTime = SimpleTimeMark.now()
                    handleBabySlugs()
                }
            }
            data.sendDeath()
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
        val data = SeaCreatureData(isOwn, seaCreature, mob.entityId, time, mob)
        seaCreatures[mob] = data
        entityIdToData[mob.entityId] = data
        SeaCreatureEvent.Spawn(data).post()
    }

    private fun handleOwnMob() {
        if (lastSeaCreatureFished.passedSince() > 1.seconds) return
        val name = lastNameFished ?: return
        val lastBobber = lastBobberLocation ?: return
        // TODO: create a sortedByFiltered function that removes elements when the comparator returns null
        val mobs = recentMobs.asSequence().filter { (mob, data) -> mob.name == name && data.passedSince() < 1.5.seconds }.map {
            it to it.key.baseEntity.distanceTo(lastBobber)
        }.filter { it.second <= 3 }
            .sortedBy { it.second }
            .take(mobsToFind).toList()

        if (mobs.isEmpty()) return
        mobsToFind -= mobs.size
        for ((entry, _) in mobs) {
            val mob = entry.key
            val time = mob.baseEntity.spawnTime
            addMob(mob, time, isOwn = true)
            recentMobs.remove(mob)
        }
        if (mobsToFind == 0) {
            lastNameFished = null
            lastBobberLocation = null
        }
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
            val mob = entry.key
            val time = mob.baseEntity.spawnTime
            addMob(mob, time, isOwn = true, isBabySlug = true)
            recentBabyMagmaSlugs.remove(mob)
        }
        if (babyMagmaSlugsToFind == 0) {
            lastMagmaSlugLocation = null
        }
    }

    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.HIGHEST)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        for (data in seaCreatures.values) data.update(event)
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTick() {
        recentMobs.removeIf { (mob, time) ->
            if (time.passedSince() < 1.2.seconds) return@removeIf false
            addMob(mob, time, isOwn = false)
            return@removeIf true
        }
        recentBabyMagmaSlugs.removeIf { (mob, time) ->
            if (time.passedSince() < 1.2.seconds) return@removeIf false
            addMob(mob, time, isOwn = false, isBabySlug = true)
            return@removeIf true
        }
        if (babyMagmaSlugsToFind != 0 && lastMagmaSlugTime.passedSince() > 2.seconds) babyMagmaSlugsToFind = 0
        val bobber = FishingApi.bobber ?: return
        lastBobberLocation = bobber.getLorenzVec()
    }

    // This should hopefully make it so that if a sea creature dies while the player isn't in the area and the despawn timer
    // isn't up yet, it will be assumed that it died
    @HandleEvent(onlyOnSkyblock = true)
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!event.repeatSeconds(5)) return
        val playerPos = McPlayer.pos
        for ((_, data) in entityIdToData) {
            if (data.isLoaded()) continue
            val lastPos = data.actualLastPos
            if (lastPos.distance(playerPos) > 20) continue
            val timeUntil = data.despawnTime.timeUntil()
            if (timeUntil < 10.seconds) continue
            data.sendDeath(false)
        }
    }

    @HandleEvent
    fun onWorldChange() = reset()

    val BABY_MAGMA_SLUG = SeaCreature(
        "Baby Magma Slug",
        fishingExperience = 730,
        chatColor = "§c",
        rare = false,
        rarity = LorenzRarity.RARE,
    )

    private fun reset() {
        entityIdToData.values.forEach { it.forceRemove() }
        entityIdToData.clear()
        seaCreatures.clear()
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

    @HandleEvent
    fun onCommand(event: BrigadierRegisterEvent) {
        event.register("nautilusresetdata") {
            this.aliases = listOf("ntresetdata")
            this.description = "Resets Sea Creature Data"
            this.category = CommandCategory.DEVELOPER_TEST
            callback { reset() }
        }
    }

    @HandleEvent
    fun onDebug(event: NautilusDebugEvent) {
        event.title("Nautilus Sea Creatures")
        event.addIrrelevant(
            "entityIdToData" to entityIdToData.entries,
            "seaCreatures" to seaCreatures.entries,
            "lastNameFished" to lastNameFished,
            "mobsToFind" to mobsToFind,
            "lastSeaCreatureFished" to lastSeaCreatureFished,
            "recentMobs" to recentMobs.entries,
            "lastBobberLocation" to lastBobberLocation,
            "babyMagmaSlugsToFind" to babyMagmaSlugsToFind,
            "lastMagmaSlugLocation" to lastMagmaSlugLocation,
            "lastMagmaSlugTime" to lastMagmaSlugTime,
            "recentBabyMagmaSlugs" to recentBabyMagmaSlugs.entries,
        )
    }


}
