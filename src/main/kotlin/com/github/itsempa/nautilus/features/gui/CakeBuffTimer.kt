package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.utils.EnumUtils.toFormattedName
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.TimeUtils.format
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.core.NautilusStorage
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import me.owdding.ktmodules.Module
import kotlin.time.Duration.Companion.days

@Module
object CakeBuffTimer {

    private val config get() = Nautilus.feature.gui
    private val storage get() = NautilusStorage.profile.centuryCakeBuffs

    private val pattern = "^§d§l(?:Big )?Yum! §r§eYou (?:gain|refresh) (?:§.)*\\+\\d+. (?<stat>.+?) (?:§.)*for".toPattern()

    enum class CenturyCakeBuffs {
        PET_LUCK,
        HEALTH,
        STRENGTH,
        FEROCITY,
        SPEED,
        DEFENSE,
        INTELLIGENCE,
        SEA_CREATURE_CHANCE,
        MAGIC_FIND,
        FARMING_FORTUNE,
        FORAGING_FORTUNE,
        MINING_FORTUNE,
        VITALITY,
        TRUE_DEFENSE,
        COLD_RESISTANCE,
        RIFT_TIME,
        ;

        var endTime: SimpleTimeMark
            get() = storage[this] ?: SimpleTimeMark.farPast()
            set(value) {
                storage[this] = value
            }


        private val formattedName = toFormattedName()
        override fun toString() = formattedName

        companion object {
            val amount = entries.size
            fun getByName(name: String): CenturyCakeBuffs? = entries.find { it.formattedName == name }
        }
    }

    private val DURATION = 2.days

    private var display: String? = null

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        pattern.findMatcher(event.message) {
            val statName = group("stat")
            val buff = CenturyCakeBuffs.getByName(statName) ?: error("stat $statName not found")
            buff.endTime = DURATION.fromNow()
            update()
        }
    }

    private fun displayNoCakesActive() {
        display = "§cNo cakes active"
    }

    private fun update() {
        if (storage.isEmpty()) return displayNoCakesActive()
        val activeBuffs = storage.filter { it.value.isInFuture() }
        if (activeBuffs.isEmpty()) return displayNoCakesActive()
        val endTime = activeBuffs.values.min()
        val amountActive = activeBuffs.size
        display = buildString {
            append("§dCake Timer: ")
            append("§b${endTime.timeUntil().format(showMilliSeconds = false, maxUnits = 2)}")
            if (amountActive < CenturyCakeBuffs.amount) {
                append("§7 ($amountActive/${CenturyCakeBuffs.amount})")
            }
        }
    }

    @HandleEvent
    fun onProfileChange(event: ProfileJoinEvent) = update()

    @HandleEvent(onlyOnSkyblock = true)
    fun onSecondPassed() = update()

    @HandleEvent(onlyOnSkyblock = true)
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.cakeBuffTimer) return
        config.cakeBuffTimerPos.renderString(display, posLabel = "Cake  Buff Timer")
    }

    @HandleEvent
    fun onDebug(event: NautilusDebugEvent) {
        event.title("Cake Buff Timer")
        event.addIrrelevant(
            "storage" to storage.entries,
            "display" to display,
            "activeBuffs" to storage.filter { it.value.isInFuture() },
        )
    }

}
