package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.entity.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.compat.getStandHelmet
import at.hannibal2.skyhanni.utils.getLorenzVec
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.events.VanquisherEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.hasDied
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.spawnTime
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Items
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Module
object VanquisherApi {

    data class VanquisherData(
        val isOwn: Boolean,
        val mob: Mob,
        val spawnTime: SimpleTimeMark,
    )

    private val spawnPattern = "§aA §r§cVanquisher §r§ais spawning nearby!".toPattern()

    private var lastOwnVanqTime = SimpleTimeMark.farPast()
    private var vanqSpawnEntity: EntityArmorStand? = null

    private var lastPossibleVanqSpawnEntity: EntityArmorStand? = null

    private var lastVanqSpawnEntityPos: LorenzVec? = null
    private var lastVanqSpawnEntityTime = SimpleTimeMark.farPast()
    private var lastVanqSoundPos: LorenzVec? = null
    private var lastVanqSoundTime = SimpleTimeMark.farPast()

    private val vanquishers = TimeLimitedCache<Mob, VanquisherData>(6.minutes) { mob, data, _ ->
        if (mob != null && data != null) VanquisherEvent.DeSpawn(data).post()
    }

    @HandleEvent(onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onChat(event: SkyHanniChatEvent) {
        if (spawnPattern.matches(event.message)) {
            lastOwnVanqTime = SimpleTimeMark.now()
            VanquisherEvent.OwnSpawn.post()
            DelayedRun.runNextTick(::handleOwnVanq)
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onSound(event: PlaySoundEvent) {
        if (event.soundName != "mob.wither.spawn" || event.pitch != 1f || event.volume != 2f) return
        lastVanqSoundPos = event.location
        lastVanqSoundTime = SimpleTimeMark.now()
        DelayedRun.runNextTick(::handleOwnVanq)
    }

    @HandleEvent(onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onEntityHealthUpdate(event: EntityMaxHealthUpdateEvent) {
        val entity = event.entity as? EntityArmorStand ?: return
        val helmet = entity.getStandHelmet() ?: return
        if (helmet.item != Items.skull || helmet.metadata != 1) return // wither skeleton skull
        lastVanqSpawnEntityPos = entity.getLorenzVec()
        lastPossibleVanqSpawnEntity = entity
        lastVanqSpawnEntityTime = SimpleTimeMark.now()
        DelayedRun.runNextTick(::handleOwnVanq)
    }

    private fun handleOwnVanq() {
        val soundPos = lastVanqSoundPos ?: return
        val entityPos = lastVanqSpawnEntityPos ?: return
        val entity = lastPossibleVanqSpawnEntity ?: return
        val now = SimpleTimeMark.now()
        if (now - lastVanqSoundTime > 2.seconds) return
        if (now - lastVanqSpawnEntityTime > 2.seconds) return
        if (now - lastOwnVanqTime > 2.seconds) return
        if (soundPos.distance(entityPos) > 3) return
        vanqSpawnEntity = entity
        lastVanqSpawnEntityPos = null
        lastVanqSoundPos = null
    }

    @HandleEvent(onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        val mob = event.mob
        if (mob.name != "Vanquisher") return
        val isOwn = mob.isOwnVanq()
        val spawnTime = mob.baseEntity.spawnTime
        val data = VanquisherData(isOwn, mob, spawnTime)
        vanquishers[mob] = data
        VanquisherEvent.Spawn(data).post()
    }

    @HandleEvent(onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onMobDeSpawn(event: MobEvent.DeSpawn.SkyblockMob) {
        val mob = event.mob
        val data = vanquishers.remove(mob) ?: return
        if (mob.hasDied) VanquisherEvent.Death(data).post()
    }

    @HandleEvent(onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onSecondPassed(event: SecondPassedEvent) {
        if ((lastPossibleVanqSpawnEntity != null || lastVanqSpawnEntityPos != null || lastVanqSoundPos != null) &&
            lastOwnVanqTime.passedSince() > 5.seconds) {
            lastPossibleVanqSpawnEntity = null
            lastVanqSpawnEntityPos = null
            lastVanqSoundPos = null
        }

        if (vanqSpawnEntity != null && lastOwnVanqTime.passedSince() > 8.seconds) {
            vanqSpawnEntity = null
        }

    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        vanquishers.clear()
        lastPossibleVanqSpawnEntity = null
        lastVanqSpawnEntityPos = null
        lastVanqSoundPos = null
        vanqSpawnEntity = null
    }

    private fun Mob.isOwnVanq(): Boolean {
        val spawnEntity = vanqSpawnEntity ?: return false
        if (baseEntity.distanceTo(spawnEntity) > 4) return false
        if (lastOwnVanqTime.passedSince() > 7.seconds) return false // TODO: actually get good time
        return true
    }

    @HandleEvent
    fun onDebug(event: NautilusDebugEvent) {
        event.title("NautilusApi")
        event.addIrrelevant(
            "vanquishers" to vanquishers,
            "lastOwnVanqTime" to lastOwnVanqTime,
            "vanqSpawnEntity" to vanqSpawnEntity,
            "lastPossibleVanqSpawnEntity" to lastPossibleVanqSpawnEntity,
            "lastVanqSpawnEntityPos" to lastVanqSpawnEntityPos,
            "lastVanqSpawnEntityTime" to lastVanqSpawnEntityTime,
            "lastVanqSoundPos" to lastVanqSoundPos,
            "lastVanqSoundTime" to lastVanqSoundTime,
        )
    }

}
