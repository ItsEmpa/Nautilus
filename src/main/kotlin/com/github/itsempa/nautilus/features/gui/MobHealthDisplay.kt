package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.SeaCreatureData
import com.github.itsempa.nautilus.data.SeaCreatureDetectionApi
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.utils.NautilusUtils.toSet
import me.owdding.ktmodules.Module

@Module
object MobHealthDisplay {

    private val config get() = Nautilus.feature.gui.healthDisplay

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

    private fun formatHealth(mob: Mob): String {
        val health = mob.health
        val maxHealth = mob.maxHealth
        val percentage = (health / maxHealth * 100)
        val color = when (percentage) {
            in 0f..config.redPercentage -> "§c"
            in config.redPercentage..50f -> "§e"
            else -> "§a"
        }
        return "$color${health.shortFormat()}§f/§a${maxHealth.shortFormat()}§c❤"
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderWorld(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.enabled) return
        if (healthMap.isEmpty()) return
        val strings = buildList {
            for ((seaCreature, health) in healthMap) {
                if (health == -1) continue
                val mob = seaCreature.mob ?: continue
                val color = if (seaCreature.isOwn) "§a" else "§c"
                add("$color${seaCreature.name} ${formatHealth(mob)}")
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
        SeaCreatureDetectionApi.getSeaCreatures().forEach(MobHealthDisplay::addMob)
    }

    private fun updateNames() {
        names = config.names.toSet(false)
    }

}
