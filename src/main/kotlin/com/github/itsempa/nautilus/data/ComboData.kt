package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.utils.EnumUtils.toFormattedName
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.addOrPut
import com.github.itsempa.nautilus.events.KillEvent
import com.github.itsempa.nautilus.events.combo.ComboEndEvent
import com.github.itsempa.nautilus.events.combo.ComboUpdateEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.fullEnumMapOf
import com.github.itsempa.nautilus.utils.replaceAll
import java.util.regex.Matcher
import kotlin.time.Duration.Companion.seconds

@Module
object ComboData {

    enum class ComboBuff(color: LorenzColor, private val icon: Char, chatName: String? = null) {
        MAGIC_FIND(LorenzColor.AQUA, '✯'),
        COINS(LorenzColor.GOLD, '⛁', chatName = "coins per kill"),
        COMBAT_WISDOM(LorenzColor.DARK_AQUA, '☯'),
        ;

        fun format(amount: Int): String = "§8+$colorCode$amount$icon"

        private val colorCode: String = color.getChatColor()
        val chatName: String = chatName ?: toFormattedName()

        companion object {
            fun getByName(name: String): ComboBuff? = entries.find { it.chatName == name }
        }
    }

    private val comboPattern =
        "§(?<color>.)§l\\+(?<combo>[\\d,.]+) Kill Combo(?: (?:§.)*\\+(?:§.)*(?<buffAmount>\\d+)\\S? (?:§.)*(?:✯ )?(?<buff>.+))?".toPattern()

    private val comboEndPattern =
        "§cYour Kill Combo has expired! You reached a (?<combo>[\\d,.]+) Kill Combo!".toPattern()

    var combo: Int = 0
        private set

    var lastComboMessage: Int = 0
        private set

    var lastUpdateTime: SimpleTimeMark = SimpleTimeMark.farPast()
        private set

    private val currentBuffs = fullEnumMapOf<ComboBuff, Int>(0)
    val buffs: Map<ComboBuff, Int> get() = currentBuffs

    var currentColor: Char = 'f'
        private set

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        val message = event.message
        comboPattern.matchMatcher(message) {
            lastUpdateTime = SimpleTimeMark.now()
            currentColor = group("color").single()
            combo = group("combo").formatInt()
            lastComboMessage = combo
            handleComboBuff()
            post(true)
            return
        }
        comboEndPattern.matchMatcher(message) {
            val combo = group("combo").formatInt()
            ComboEndEvent(combo).post()
            this@ComboData.reset()
        }
    }

    @HandleEvent
    fun onKill(event: KillEvent) {
        val newComboAmount = combo + event.kills
        val nextComboMessage = nextComboMessage(combo)
        if (nextComboMessage <= newComboAmount) return
        combo = newComboAmount
        post(false)
    }

    @HandleEvent
    fun onWorldChange() {
        if (combo != 0) {
            ComboEndEvent(combo).post()
            reset()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onSecondPassed() {
        if (lastUpdateTime.passedSince() < 10.seconds) return
        if (combo == 0 || combo > 5) return
        ComboEndEvent(combo).post()
        reset()
    }

    private fun reset() {
        lastUpdateTime = SimpleTimeMark.now()
        currentColor = 'f'
        combo = 0
        lastComboMessage = 0
        currentBuffs.replaceAll(0)
        post(true)
    }

    private fun post(fromChat: Boolean) = ComboUpdateEvent(combo, currentColor, currentBuffs, fromChat).post()

    private fun Matcher.handleComboBuff() {
        val buffName = groupOrNull("buff") ?: return
        val buff = ComboBuff.getByName(buffName) ?: return
        val buffAmount = groupOrNull("buffAmount")?.formatInt() ?: return
        currentBuffs.addOrPut(buff, buffAmount)
    }

    fun nextComboMessage(number: Int): Int {
        val divisor = if (number < 30) 5 else 25
        return (number / divisor) * divisor + divisor
    }
}
