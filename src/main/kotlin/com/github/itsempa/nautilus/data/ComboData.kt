package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.utils.EnumUtils.toFormattedName
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.addOrPut
import com.github.itsempa.nautilus.events.ComboUpdateEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.fullEnumMapOf
import com.github.itsempa.nautilus.utils.replaceAll
import java.util.regex.Matcher

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
        "§cYour Kill Combo has expired! You reached a [\\d,.]+ Kill Combo!".toPattern()

    var combo: Int = 0
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
            handleComboBuff()
            post()
            return
        }
        if (comboEndPattern.matches(message)) return reset()
    }

    @HandleEvent
    fun onWorldChange() {
        if (combo != 0) reset()
    }

    private fun reset() {
        lastUpdateTime = SimpleTimeMark.now()
        currentColor = 'f'
        combo = 0
        currentBuffs.replaceAll(0)
        post()
    }

    private fun post() = ComboUpdateEvent(combo, currentColor, currentBuffs).post()

    private fun Matcher.handleComboBuff() {
        val buffName = groupOrNull("buff") ?: return
        val buff = ComboBuff.getByName(buffName) ?: return
        val buffAmount = groupOrNull("buffAmount")?.formatInt() ?: return
        currentBuffs.addOrPut(buff, buffAmount)
    }
}
