package com.github.itsempa.nautilus.features.render

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.TimeUtils.format
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.SeaCreatureData
import com.github.itsempa.nautilus.data.VanquisherApi
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.events.VanquisherEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.getLorenzVec
import com.github.itsempa.nautilus.utils.NautilusUtils.getHeight
import kotlin.time.Duration.Companion.seconds

@Module
object InvincibilityTimer {

    private val config get() = Nautilus.feature.render.invincibility

    private val seaCreatures = mutableSetOf<SeaCreatureData>()
    private val vanquishers = mutableSetOf<VanquisherApi.VanquisherData>()
    private val INVINCIBILITY = 5.seconds

    @HandleEvent
    fun onSeaCreatureSpawn(event: SeaCreatureEvent.Spawn) {
        val seaCreature = event.seaCreature
        if (!seaCreature.rarity.isAtLeast(LorenzRarity.LEGENDARY)) return
        seaCreatures.add(seaCreature)
    }

    @HandleEvent
    fun onVanquisherSpawn(event: VanquisherEvent.Spawn) = vanquishers.add(event.data)

    @HandleEvent
    fun onVanquisherDeSpawn(event: VanquisherEvent.DeSpawn) = vanquishers.add(event.data)

    @HandleEvent
    fun onSeaCreatureDeSpawn(event: SeaCreatureEvent.Remove) = seaCreatures.remove(event.seaCreature)

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!config) return
        for (seaCreature in seaCreatures) {
            if (!seaCreature.isLoaded()) continue
            val height = seaCreature.aabb?.getHeight() ?: continue
            val pos = seaCreature.pos?.up(height + 1.5) ?: continue
            val time = seaCreature.spawnTime + INVINCIBILITY
            if (time.passedSince() > 1.seconds) continue
            val timeLeft = time.timeUntil()
            event.drawString(pos, "§b${timeLeft.format(showMilliSeconds = true)}")
            if (!seaCreature.isOwn) continue
            event.drawString(pos.up(0.5), "§aOWN MOB")
        }
        for (vanquisher in vanquishers) {
            val time = vanquisher.spawnTime + INVINCIBILITY
            if (time.passedSince() > 1.seconds) continue
            val mob = vanquisher.mob
            val height = mob.baseEntity.height
            val pos = vanquisher.mob.getLorenzVec().up(height + 1.5)
            val timeLeft = time.timeUntil()
            event.drawString(pos, "§b${timeLeft.format(showMilliSeconds = true)}")
            if (!vanquisher.isOwn) continue
            event.drawString(pos.up(0.5), "§aOWN MOB")

        }
    }

}
