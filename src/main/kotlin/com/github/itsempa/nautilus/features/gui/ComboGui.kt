package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.TimeUtils.format
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.ComboData
import com.github.itsempa.nautilus.data.KillData
import com.github.itsempa.nautilus.events.combo.ComboUpdateEvent
import me.owdding.ktmodules.Module
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Module
object ComboGui {

    private val config get() = Nautilus.feature.gui.combo
    private var nextComboTimer = SimpleTimeMark.farPast()

    private var buffDisplay: String? = null
    private var timeDisplay: String? = null

    @HandleEvent
    fun onComboUpdate(event: ComboUpdateEvent) {
        with(event) {
            updateTime(combo)
            updateTimeDisplay()
            update(combo, colorCode, buffs)
        }
    }

    private fun update(combo: Int, colorCode: Char, buffs: Map<ComboData.ComboBuff, Int>) {
        buffDisplay = if (combo == 0) null
        else "§$colorCode§l+$combo §r${buffs.entries.joinToString(" ") { (buff, amount) -> buff.format(amount) }}"
    }

    private fun updateTime(combo: Int) {
        nextComboTimer = if (combo == 0) SimpleTimeMark.farPast()
        else getComboDuration(combo, config.grandmaWolfLevel).fromNow()
    }

    private fun updateTimeDisplay() {
        timeDisplay = if (nextComboTimer.isFarPast() || KillData.lastKill.passedSince() > 15.seconds) null
        else {
            val time = nextComboTimer.timeUntil()
            if (time < (-1).seconds) null
            else " §b(${time.format(showMilliSeconds = true)})"
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTick() = updateTimeDisplay()

    @HandleEvent(onlyOnSkyblock = true)
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.enabled) return
        val buffDisplay = buffDisplay ?: return
        val display = buffDisplay + timeDisplay.orEmpty()
        config.pos.renderString(display, posLabel = "Combo Gui")
    }

    private fun getComboDuration(combo: Int, petLevel: Int): Duration {
        val nextMessage = ComboData.nextComboMessage(combo).coerceAtMost(30)
        val (baseTime, timePerLevel) = when (nextMessage) { // grandma wolf duration
            5 -> 8 to 0.02
            10 -> 6 to 0.02
            15 -> 4 to 0.02
            20 -> 3 to 0.02
            25 -> 3 to 0.01
            else -> 2 to 0.01
        }
        val level = petLevel.coerceIn(1..100)
        return (baseTime + timePerLevel * level).seconds
    }

}
