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
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.compat.getStandHelmet
import com.github.itsempa.nautilus.events.VanquisherEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChatUtils
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.hasDied
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.spawnTime
import net.minecraft.entity.item.EntityArmorStand
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
    private var lastOwnVanqSpawnPos: LorenzVec? = null
    private var vanquisherAmount = 0

    private val vanquishers = TimeLimitedCache<Mob, VanquisherData>(6.minutes) { mob, data, _ ->
        if (mob != null && data != null) VanquisherEvent.DeSpawn(data).post()
    }

    @HandleEvent(onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onChat(event: SkyHanniChatEvent) {
        if (spawnPattern.matches(event.message)) {
            lastOwnVanqTime = SimpleTimeMark.now()
            ++vanquisherAmount
            VanquisherEvent.OwnSpawn.post()
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onSound(event: PlaySoundEvent) {
        if (event.soundName != "mob.wither.spawn" || event.pitch != 1f || event.volume != 2f) return
        NautilusChatUtils.debug("Played Vanquisher spawn sound, last spawn: ${lastOwnVanqTime.passedSince().format()}")
    }

    @HandleEvent(onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onEntityHealthUpdate(event: EntityMaxHealthUpdateEvent) {
        val entity = event.entity as? EntityArmorStand ?: return
        val helmet = entity.getStandHelmet() ?: return
        // TODO: get what item vanquisher entity armor stand animation has as helmet

    }

    private fun handleOwnVanq() {

    }

    @HandleEvent(onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        val mob = event.mob
        if (mob.name != "Vanquisher") return
        val isOwn = mob.isOwnVanq()
        if (isOwn) --vanquisherAmount
        val data = VanquisherData(isOwn, mob, mob.baseEntity.spawnTime)
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
        if (vanquisherAmount == 0 && lastOwnVanqTime.passedSince() < 15.seconds) return
        vanquisherAmount = 0
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        vanquishers.clear()
        vanquisherAmount = 0
    }

    @Suppress("UnusedReceiverParameter")
    private fun Mob.isOwnVanq(): Boolean {
        if (vanquisherAmount <= 0) return false
        if (lastOwnVanqTime.passedSince() > 10.seconds) return false // TODO: actually get good time
        // TODO: check position somehow?
        return true
    }

}
