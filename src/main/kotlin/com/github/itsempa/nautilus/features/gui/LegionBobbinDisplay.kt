package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.mob.MobFilter.isRealPlayer
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getHypixelEnchantments
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat.isLocalPlayer
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableString
import at.hannibal2.skyhanni.utils.renderables.container.HorizontalContainerRenderable
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusItemUtils.uuid
import com.github.itsempa.nautilus.utils.NautilusNullableUtils.orZero
import com.github.itsempa.nautilus.utils.helpers.McPlayer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityFishHook
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Module
object LegionBobbinDisplay {

    private val config get() = Nautilus.feature.gui.legionBobbinDisplay

    private const val BOBBERS_DISTANCE = 30.0
    private const val BOBBERS_LIMIT = 10
    private const val BOBBIN_MULT = 0.16
    private const val MAX_BOBBIN_LVL = 5

    private const val LEGION_DISTANCE = 30.0
    private const val LEGION_LIMIT = 20
    private const val LEGION_MULT = 0.07
    private const val MAX_LEGION_LVL = 5

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

    private val armorDataCache = TimeLimitedCache<UUID, ArmorData>(5.seconds)

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
    fun onSecondPassed() {
        if (!isEnabled()) return
        val armor = InventoryUtils.getArmor()
        var newLegionBuff = 0.0
        var newBobbinBuff = 0.0
        for (piece in armor) {
            if (piece == null) continue
            val uuid = piece.uuid ?: continue
            val data = armorDataCache.getOrPut(uuid) {
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

    // TODO: cache renderable creation
    @HandleEvent(onlyOnSkyblock = true)
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        val renderables = createRenderable()
        config.position.renderRenderables(renderables, posLabel = "Legion Bobbin Display")
    }

    private fun createRenderable(): List<Renderable> {
        if (!config.alwaysShowMax) return createRealRenderable()

        val legionBuff = MAX_LEGION_LVL * LEGION_MULT * nearbyPlayers
        val bobbinBuff = MAX_BOBBIN_LVL * BOBBIN_MULT * nearbyBobbers


        val (legionTitle, legionText) = formatInfo(
            wearingLegion, legionBuff > bobbinBuff, nearbyPlayers, legionBuff, "§d", "Legion",
        )

        val (bobbinTitle, bobbinText) = formatInfo(
            wearingBobbin, bobbinBuff > legionBuff, nearbyBobbers, bobbinBuff, "§3", "Bobbin",
        )

        return listOf(
            HorizontalContainerRenderable(listOf(
                RenderableString(legionTitle),
                RenderableString(legionText),
            )),
            HorizontalContainerRenderable(listOf(
                RenderableString(bobbinTitle),
                RenderableString(bobbinText),
            )),
        )

    }

    private fun createRealRenderable(): List<Renderable> {
        return buildList {
            if (!config.hideWithoutEnchant || wearingLegion) add(
                HorizontalContainerRenderable(
                    listOf(
                        RenderableString("§dLegion: "),
                        RenderableString("§b$nearbyPlayers §7(${(armorLegionBuff * nearbyPlayers).roundTo(2)}%)"),
                    ),
                ),
            )
            if (!config.hideWithoutEnchant || wearingBobbin) add(
                HorizontalContainerRenderable(
                    listOf(
                        RenderableString("§3Bobbin: "),
                        RenderableString("§b$nearbyBobbers §7(${(armorBobbinBuff * nearbyBobbers).roundTo(2)}%)"),
                    ),
                ),
            )
        }
    }

    private fun formatInfo(
        isWearing: Boolean,
        isBold: Boolean,
        nearby: Int,
        buff: Double,
        color: String,
        title: String,
    ): Pair<String, String> {

        val baseColor = if (isWearing) color else "§7§o"
        val boldCode = if (isBold) "§l" else ""

        val legionTitle = buildString {
            append(baseColor)
            append(boldCode)
            append("$title: ")
        }

        val numberColor = if (isWearing) "§b" else "§7§o"
        val parenthesisColor = if (isWearing) "§7" else ""

        val legionFormat = buildString {
            append(numberColor)
            append(boldCode)
            append(nearby)
            append(" ")
            append(parenthesisColor)
            append(boldCode)
            append("(${buff.roundTo(2)}%)")
        }

        return legionTitle to legionFormat
    }

    private fun isEnabled() = config.enabled


}
