package com.github.itsempa.nautilus.features.render

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.SeaCreatureData
import com.github.itsempa.nautilus.data.SeaCreatureDetectionApi
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusUtils.toSet

@Module
object MobHealthDisplay {

    private val config get() = Nautilus.feature.render.healthDisplay

    private var names = setOf<String>()
    private val healthMap = mutableMapOf<SeaCreatureData, Int>()

    @HandleEvent
    fun onSeaCreatureSpawn(event: SeaCreatureEvent.Spawn) = addMob(event.seaCreature)

    @HandleEvent
    fun onSeaCreatureRemove(event: SeaCreatureEvent.Remove) = healthMap.remove(event.seaCreature)

    @HandleEvent(onlyOnSkyblock = true)
    fun onTick() {
        for (seaCreature in healthMap.keys) {
            if (!seaCreature.isLoaded() || !seaCreature.canBeSeen()) continue
            val health = seaCreature.health ?: continue
            if (health == -1) continue
            healthMap[seaCreature] = health
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderWorld(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.enabled) return
        if (healthMap.isEmpty()) return
        val strings = buildList {
            for ((seaCreature, health) in healthMap) {
                if (health == -1) continue
                val mob = seaCreature.mob ?: continue
                val color = if (seaCreature.isOwn) "§a" else "§b"
                add("$color${seaCreature.name} §a${mob.health.shortFormat()}§f/§a${mob.maxHealth.shortFormat()}§c❤")
                if (size >= config.limit) break
            }
        }
        config.pos.renderStrings(strings, posLabel = "Mob Health Display")
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        updateNames()
        config.names.onToggle {
            updateNames()
            reloadMobs()
        }
    }

    private fun addMob(seaCreature: SeaCreatureData) {
        if (seaCreature.name in names) {
            val value = if (seaCreature.canBeSeen()) seaCreature.health else null
            healthMap[seaCreature] = value ?: -1
        }
    }

    private fun reloadMobs() {
        healthMap.clear()
        SeaCreatureDetectionApi.getSeaCreatures().forEach(::addMob)
    }

    private fun updateNames() {
        names = config.names.toSet(false)
    }

}
