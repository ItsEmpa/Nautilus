package com.github.itsempa.nautilus.features.chat

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.PartyApi
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.data.hypixel.chat.event.PartyChatEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.fishing.SeaCreature
import at.hannibal2.skyhanni.features.fishing.SeaCreatureManager
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.hasGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.NautilusUtils.asChatMessage
import com.github.itsempa.nautilus.utils.NautilusUtils.asLorenzVec
import com.github.itsempa.nautilus.utils.NautilusUtils.asSimpleChatMessage
import com.github.itsempa.nautilus.utils.TemporaryWaypoint
import com.github.itsempa.nautilus.utils.helpers.McPlayer
import me.owdding.ktmodules.Module
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Module
object SeaCreatureWarning {

    private val config get() = Nautilus.feature.chat.seaCreatureWarning

    private val pattern = "\\[Nautilus] (?<seaCreature>.+) (?<dh>x2)? (?<pos>.+)".toPattern()

    private val waypoint = TemporaryWaypoint(5.minutes, 10.0)
    private var text: List<String>? = null

    @HandleEvent
    fun onSeaCreatureFish(event: SeaCreatureFishEvent) {
        if (!config.enabled) return
        val seaCreature = event.seaCreature
        val doubleHook = event.doubleHook
        /*if (seaCreature.rare) */showWarning(seaCreature, doubleHook, null)
        if (config.partyMessage) sendChatMessage(seaCreature, doubleHook)
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!config.enabled) return
        val pos = waypoint.getPos()?.up() ?: return
        val list = text ?: return
        var yOffset = 0f
        for (line in list) {
            event.drawDynamicText(pos, line, scaleMultiplier = 1.3, yOff = yOffset)
            yOffset += 10f
        }
        val distanceText = "§b${pos.distanceToPlayer().toInt().addSeparators()}"
        event.drawDynamicText(pos, distanceText, scaleMultiplier = 1.3, yOff = yOffset)
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: PartyChatEvent) {
        if (!config.otherSeaCreatures) return
        pattern.matchMatcher(event.message) {
            val seaCreature = SeaCreatureManager.allFishingMobs[group("seaCreature")] ?: return
            val doubleHook = hasGroup("dh")
            val pos = group("pos").asLorenzVec() ?: return
            event.blockedReason = "NT_SEA_CREATURE"
            showWarning(seaCreature, doubleHook, event.cleanedAuthor)
            waypoint.setPos(pos)
            val seaCreatureText = "${seaCreature.displayName}§e${if (doubleHook) " (x2)" else ""}"
            text = listOfNotNull(
                seaCreatureText,
                if (event.cleanedAuthor == McPlayer.name) null else "§a${event.cleanedAuthor}",
            )
            NautilusChat.hoverableChat(
                "${event.author} §ecaught $seaCreatureText at §b${pos.asSimpleChatMessage()}!",
                listOf(event.chatComponent.formattedText),
                prefix = false,
            )
        }
    }

    private fun sendChatMessage(seaCreature: SeaCreature, doubleHook: Boolean) {
        if (!PartyApi.isInParty()) return
        val message = buildString {
            append("[Nautilus] ")
            append(seaCreature.name)
            if (doubleHook) append(" (x2)")
            append(" ")
            append(McPlayer.pos.asChatMessage())
        }
        HypixelCommands.partyChat(message)
    }

    private fun showWarning(seaCreature: SeaCreature, doubleHook: Boolean, owner: String?) {
        val name = "${seaCreature.chatColor}§l${seaCreature.name.uppercase()}"
        val title = "$name${if (doubleHook) " §e(x2)" else ""}"
        val subtitle = if (owner == null) null else "§b$owner"
        TitleManager.sendTitle(title, subtitle, duration = 2.seconds)
        config.sound.playSound()
    }


}
