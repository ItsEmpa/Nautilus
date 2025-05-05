package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.modules.Module
import kotlin.time.Duration.Companion.seconds

@Module
object RainTimer {

    private val config get() = Nautilus.feature.gui.rainTimer

    private var time: SimpleTimeMark = SimpleTimeMark.farPast()

    private var lastWarning: SimpleTimeMark = SimpleTimeMark.farPast()

    @HandleEvent(onlyOnIslands = [IslandType.THE_PARK, IslandType.SPIDER_DEN])
    fun onWidget(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.RAIN)) return
        if (event.isClear()) return reset()
        val timeString = event.widget.matchMatcherFirstLine { group("time") } ?: return
        val timeLeft = timeString.removeSuffix(" left").trim()
        val duration = TimeUtils.getDuration(timeLeft)
        val newTime = duration.fromNow()
        val diff = (newTime - time).absoluteValue
        if (diff > 1.seconds) time = newTime
    }

    @HandleEvent
    fun onWorldChange() = reset()

    private fun reset() {
        time = SimpleTimeMark.farPast()
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_PARK)
    fun onSecondPassed() {
        if (!config.warnOnLow) return
        if (time.isFarPast()) return
        val timeUntil = time.timeUntil()
        if (timeUntil < config.warningTime.seconds) tryWarn()
    }

    private fun tryWarn() {
        if (lastWarning.passedSince() < 5.seconds) return
        lastWarning = SimpleTimeMark.now()
        TitleManager.sendTitle("§cRain Timer is low!")
        config.sound.playSound()
    }

    @HandleEvent(onlyOnIslands = [IslandType.THE_PARK, IslandType.SPIDER_DEN])
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.enabled) return
        val text = if (time.isInPast()) "§cNo rain"
        else "§b${time.timeUntil().format()}"
        config.position.renderString("§eRain Timer: $text", posLabel = "Rain Timer")
    }

}
