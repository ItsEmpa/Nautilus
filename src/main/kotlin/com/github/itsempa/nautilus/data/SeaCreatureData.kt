package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.fishing.SeaCreature
import at.hannibal2.skyhanni.features.fishing.SeaCreatureManager
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import com.github.itsempa.nautilus.modules.Module
import kotlin.time.Duration.Companion.minutes

@Module
object SeaCreatureData {

    data class SeaCreatureData(
        var isOwn: Boolean,
        val seaCreature: SeaCreature,
        val entityId: Int,
        val spawnTime: SimpleTimeMark = SimpleTimeMark.farPast(),
        var mob: Mob?,
        var lastKnownPos: LorenzVec,
    ) {
        val isRare: Boolean get() = seaCreature.rare

        fun update() {
            val mob = mob ?: return
        }
    }

    private val entityIdToData = TimeLimitedCache<Int, SeaCreatureData>(6.minutes)
    private val seaCreatures = mutableMapOf<Mob, SeaCreatureData>()

    private var lastSeaCreatureFished = SimpleTimeMark.farPast()
    private var lastNameFished: String? = null
    private var mobsToFind = 0

    @HandleEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        val mob = event.mob
        val data = entityIdToData[mob.id]
        if (data != null) {
            seaCreatures[mob] = data
            data.mob = mob
            data.update()
            return
        }

        if (mob.name == "Baby Magma Slug") {
            // TODO: handle magma slug
            return
        }
        if (mob.name !in SeaCreatureManager.allFishingMobs) return
        // TODO continue
    }

    @HandleEvent
    fun onMobDespawn(event: MobEvent.DeSpawn.SkyblockMob) {
        val mob = event.mob
        seaCreatures.remove(mob)
        val data = entityIdToData[mob.id]
        if (data != null) {
            data.mob = null
        }
        // TODO: handle removing entity data if can confirm that it hasnt despawned because of distance
        //  Also handle baby slugs
    }

    @HandleEvent
    fun onSeaCreatureFish(event: SeaCreatureFishEvent) {
        lastSeaCreatureFished = SimpleTimeMark.now()
        lastNameFished = event.seaCreature.name
        mobsToFind = if (event.doubleHook) 2 else 1
        handle()
    }

    private fun handle() {
        // TODO
    }

    private fun handleBabySlugs() {
        // TODO
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        entityIdToData.clear()
        seaCreatures.clear()
        lastSeaCreatureFished = SimpleTimeMark.farPast()
        lastNameFished = null
        mobsToFind = 0
    }


}
