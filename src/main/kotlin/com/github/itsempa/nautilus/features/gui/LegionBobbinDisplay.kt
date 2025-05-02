package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.mob.MobFilter.isRealPlayer
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getHypixelEnchantments
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat.isLocalPlayer
import at.hannibal2.skyhanni.utils.renderables.RenderableString
import at.hannibal2.skyhanni.utils.renderables.container.HorizontalContainerRenderable
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusNullableUtils.orZero
import com.github.itsempa.nautilus.utils.helpers.McPlayer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityFishHook
import net.minecraft.item.ItemStack
import kotlin.time.Duration.Companion.seconds

@Module
object LegionBobbinDisplay {

    private val config get() = Nautilus.feature.gui.legionBobbinDisplay

    private const val BOBBERS_DISTANCE = 30.0
    private const val BOBBERS_LIMIT = 10
    private const val BOBBIN_MULT = 0.16

    private const val LEGION_DISTANCE = 30.0
    private const val LEGION_LIMIT = 20
    private const val LEGION_MULT = 0.07

    private var nearbyBobbers: Int = 0
    private var nearbyPlayers: Int = 0
    private var armorLegionBuff: Double = 0.0
    private var armorBobbinBuff: Double = 0.0

    private val wearingLegion: Boolean get() = armorLegionBuff != 0.0
    private val wearingBobbin: Boolean get() = armorBobbinBuff != 0.0

    private data class ArmorData(
        val legion: Int,
        val bobbin: Int,
    )

    private val armorDataCache = TimeLimitedCache<ItemStack, ArmorData>(5.seconds)

    @HandleEvent(onlyOnSkyblock = true)
    fun onTick(event: SkyHanniTickEvent) {
        if (!event.isMod(2) || !isEnabled()) return
        var bobbers = 0
        var players = 0
        val playerPos = McPlayer.pos
        for (entity in EntityUtils.getAllEntities()) {
            when (entity) {
                is EntityFishHook -> {
                    if (entity.distanceTo(playerPos) > BOBBERS_DISTANCE) continue
                    ++bobbers
                }

                is EntityPlayer -> {
                    if (entity.isLocalPlayer || !entity.isRealPlayer()) continue
                    if (entity.distanceTo(playerPos) > LEGION_DISTANCE) continue
                    ++players
                }
            }
        }
        nearbyBobbers = bobbers.coerceAtMost(BOBBERS_LIMIT)
        nearbyPlayers = players.coerceAtMost(LEGION_LIMIT)
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        val armor = InventoryUtils.getArmor()
        var newLegionBuff = 0.0
        var newBobbinBuff = 0.0
        for (piece in armor) {
            if (piece == null) continue
            val data = armorDataCache.getOrPut(piece) {
                val enchants = piece.getHypixelEnchantments() ?: return@getOrPut ArmorData(0, 0)
                val legion = enchants["ultimate_legion"].orZero()
                val bobbin = enchants["ultimate_bobbin_time"].orZero()
                ArmorData(legion, bobbin)
            }
            newLegionBuff += data.legion * LEGION_MULT
            newBobbinBuff += data.bobbin * BOBBIN_MULT
        }
        armorLegionBuff = newLegionBuff
        armorBobbinBuff = newBobbinBuff
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        val renderables = buildList {
            if (!config.hideWithoutEnchant || wearingLegion) add(
                HorizontalContainerRenderable(listOf(
                    RenderableString("§dLegion: "),
                    RenderableString("§b${armorLegionBuff * nearbyPlayers}% §7($nearbyPlayers)"),
                )),
            )
            if (!config.hideWithoutEnchant || wearingBobbin) add(
                HorizontalContainerRenderable(listOf(
                    RenderableString("§3Bobbin: "),
                    RenderableString("§b${armorBobbinBuff * nearbyBobbers}% §7($nearbyBobbers)"),
                )),
            )
        }
        config.position.renderRenderables(renderables, posLabel = "Legion Bobbin Display")
    }

    private fun isEnabled() = config.enabled


}
