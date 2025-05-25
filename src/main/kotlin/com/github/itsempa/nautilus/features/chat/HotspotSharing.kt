package com.github.itsempa.nautilus.features.chat

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.PartyApi
import at.hannibal2.skyhanni.data.hypixel.chat.event.AbstractChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.PartyChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.PlayerAllChatEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.chat.TextHelper
import at.hannibal2.skyhanni.utils.chat.TextHelper.asComponent
import at.hannibal2.skyhanni.utils.chat.TextHelper.onClick
import at.hannibal2.skyhanni.utils.chat.TextHelper.onHover
import at.hannibal2.skyhanni.utils.chat.TextHelper.send
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.HotspotApi
import com.github.itsempa.nautilus.events.HotspotEvent
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.NautilusRenderUtils.drawBoundingBox
import com.github.itsempa.nautilus.utils.NautilusUtils.asChatMessage
import com.github.itsempa.nautilus.utils.NautilusUtils.asLorenzVec
import com.github.itsempa.nautilus.utils.NautilusUtils.asSimpleChatMessage
import com.github.itsempa.nautilus.utils.NautilusUtils.getBlockAABB
import com.github.itsempa.nautilus.utils.errorIfNull
import com.github.itsempa.nautilus.utils.helpers.McPlayer
import me.owdding.ktmodules.Module
import net.minecraft.util.IChatComponent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Suppress("UnstableApiUsage")
@Module
object HotspotSharing {

    private const val MAX_DISTANCE = 20

    private val config get() = Nautilus.feature.chat.hotspotSharing

    private val coordMessagePattern = "\\[Nautilus] Hotspot with (?<buff>.+) buff at (?<coords>.+)".toPattern()

    private val recentHotspots = TimeLimitedCache<LorenzVec, HotspotApi.HotspotBuff>(5.minutes)
    private var waypoint: Pair<LorenzVec, HotspotApi.HotspotBuff>? = null
    private var lastWaypointSet = SimpleTimeMark.farPast()

    @HandleEvent
    fun onHotspotBuff(event: HotspotEvent.BuffFound) {
        val hotspot = event.hotspot
        if (hotspot.hasBeenSeen) handleHotspot(hotspot)
    }

    @HandleEvent
    fun onPartyChat(event: PartyChatEvent) = handleChatEvent(event)

    @HandleEvent
    fun onAllChat(event: PlayerAllChatEvent) = handleChatEvent(event)

    private fun handleChatEvent(event: AbstractChatEvent) {
        if (!config.enabled) return
        coordMessagePattern.matchMatcher(event.message) {
            val buff = HotspotApi.HotspotBuff.getByStatName(group("buff"))
            val coords = group("coords").asLorenzVec() ?: return@matchMatcher
            val isAlreadyDetected = coords.anyHotspotNearby()
            if (!isAlreadyDetected) recentHotspots[coords] = buff
            event.blockedReason = "NT_HOTSPOT"
            NautilusChat.hoverableChat(
                "${event.author} §efound a hotspot with §c${buff.statName} §ebuff at §b${coords.asSimpleChatMessage()}!",
                listOf(event.chatComponent.formattedText),
                prefix = false
            )
            val currentWaypoint = waypoint
            val buffIndex = config.buffs.indexOf(buff)
            if (buffIndex == -1) return // we use the index later, so we can just use this here instead of calculating contains again
            val shouldUpdate = currentWaypoint == null ||
                (config.buffs.indexOf(currentWaypoint.second) < buffIndex && currentWaypoint.first.distance(coords) > MAX_DISTANCE)
            if (shouldUpdate) {
                waypoint = coords to buff
                lastWaypointSet = SimpleTimeMark.now()
                if (event.author != McPlayer.self.name) config.sound.playSound()
            }
        }
    }

    private fun LorenzVec.anyHotspotNearby(): Boolean = recentHotspots.keys.any { distance(it) < MAX_DISTANCE }

    @HandleEvent
    fun onHotspotSeen(event: HotspotEvent.Seen) {
        val hotspot = event.hotspot
        if (hotspot.buff != null) handleHotspot(hotspot)
    }

    @HandleEvent
    fun onSecondPassed() {
        val (pos, _) = waypoint ?: return
        val lastSpot = HotspotApi.lastHotspotPos
        if (pos.distanceToPlayer() < 10 ||
            lastWaypointSet.passedSince() > 1.minutes ||
            (lastSpot != null && (HotspotApi.lastHotspotFish.passedSince() < 5.seconds) && lastSpot.distance(pos) < 10)
        ) {
            waypoint = null
        }
    }

    @HandleEvent
    fun onWorldChange() {
        recentHotspots.clear()
        waypoint = null
        lastWaypointSet = SimpleTimeMark.farPast()
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!config.enabled) return
        val (pos, buff) = waypoint ?: return
        event.drawBoundingBox(pos.getBlockAABB(), LorenzColor.BLUE.toColor(), throughBlocks = true)
        event.drawString(pos.up(2), buff.toString(), seeThroughBlocks = true)
        val distance = pos.distanceToPlayer().toInt().addSeparators()
        event.drawString(pos.up(), "§b${distance}m", seeThroughBlocks = true)
    }

    private fun handleHotspot(hotspot: HotspotApi.Hotspot) {
        if (!config.enabled) return
        if (hotspot.center.anyHotspotNearby()) return
        val buff = errorIfNull(hotspot.buff) { "Hotspot buff should not be null at this point." }
        if (buff !in config.buffs) return
        val instantParty = config.instantPartyChat
        val inParty = PartyApi.isInParty()
        recentHotspots[hotspot.center] = buff
        val baseMessage = NautilusChat.prefixComponent("Found Hotspot with §c${buff.statName}§3 buff!")
        val list = buildList {
            if (!instantParty && inParty) {
                add(TextHelper.text("§9§n[PARTY]") {
                    onClick { sendPartyChat(hotspot) }
                    onHover("Click to send to §9party!")
                })
            }
            if (config.allChat) {
                add(TextHelper.text("§e§n[ALL]") {
                    onClick { sendAllChat(hotspot) }
                    onHover("Click to send to §eall!")
                })
            }
        }
        val finalComponent: IChatComponent
        if (list.isNotEmpty()) {
            val separator = "§7 - ".asComponent()
            val buttons = TextHelper.join(list, separator = separator)
            finalComponent = TextHelper.multiline(baseMessage, buttons)
        } else {
            finalComponent = baseMessage
        }
        finalComponent.send()
        if (instantParty && inParty) sendPartyChat(hotspot)
    }

    private fun sendPartyChat(hotspot: HotspotApi.Hotspot) = HypixelCommands.partyChat(getChatMessage(hotspot))

    private fun sendAllChat(hotspot: HotspotApi.Hotspot) = HypixelCommands.allChat(getChatMessage(hotspot))

    private fun getChatMessage(hotspot: HotspotApi.Hotspot): String =
        "[Nautilus] Hotspot with ${hotspot.buff?.statName ?: "Unknown"} buff at ${hotspot.center.asChatMessage()}"

    @HandleEvent
    fun onDebug(event: NautilusDebugEvent) {
        event.title("Hotspot Sharing")
        event.addIrrelevant(
            "recentHotspots" to recentHotspots.entries,
            "waypoint" to waypoint,
            "lastWaypointSet" to lastWaypointSet,
        )
    }

}
