package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import com.github.itsempa.nautilus.events.RareDropEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.tryOrDefault

enum class RareDropType {
    RARE_DROP,
    VERY_RARE_DROP,
    CRAZY_RARE_DROP,
    INSANE_DROP,
    PET_DROP,
    UNKNOWN,
    ;

    companion object {
        fun fromName(name: String) = tryOrDefault(UNKNOWN) { valueOf(name) }
    }
}

enum class RareDropStat(val icon: Char) {
    MAGIC_FIND('✯'),
    FARMING_FORTUNE('☘'),
    ;

    companion object {
        fun fromIcon(icon: Char) = entries.find { it.icon == icon }
    }
}

@Module
object RareDropManager {

    private val regex =
        "^§.§l(?<dropType>[\\w\\s]+ DROP)! (?:(?:§.)*\\((?:§.)*)?(?:(?<amount>[\\d,.]+)x )?(?:§r)?(?<item>.+?)(?:§.)*\\)?(?: (?:§.)*\\((?:§.)*\\+(?:§.)*(?<mf>[\\d,.]+)(?:% (?:§.)*)?(?<icon>[☘✯]).*\\))? ?(?:§.)*\$".toPattern()

    @HandleEvent(onlyOnSkyblock = true, receiveCancelled = true)
    fun onChat(event: SkyHanniChatEvent) {
        regex.matchMatcher(event.message) {
            val item = NeuInternalName.fromItemNameOrNull(group("item")) ?: return
            val dropType = RareDropType.fromName(group("dropType"))
            val amount = groupOrNull("amount")?.formatInt() ?: 1
            val magicFind = groupOrNull("mf")?.formatInt()
            val icon = groupOrNull("icon")?.firstOrNull()
            val dropStat = if (icon != null) RareDropStat.fromIcon(icon) else null
            RareDropEvent(item, amount, dropType, magicFind, dropStat).post()
        }
    }


}
