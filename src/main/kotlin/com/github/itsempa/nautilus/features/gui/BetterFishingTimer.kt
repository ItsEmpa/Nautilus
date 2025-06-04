package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.SeaCreatureData
import com.github.itsempa.nautilus.data.SeaCreatureDetectionApi
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.utils.NautilusNullableUtils.orFarPast
import me.owdding.ktmodules.Module
import kotlin.reflect.KMutableProperty0
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Module
object BetterFishingTimer {

    private val config get() = Nautilus.feature.gui.fishingTimer

    private const val GLOBAL_CAP = 60
    private val warningDelay = 5.seconds

    private enum class FishingCap(val island: IslandType, personalCap: Int? = null) {
        CRIMSON_ISLE(IslandType.CRIMSON_ISLE, 5),
        CRYSTAL_HOLLOWS(IslandType.CRYSTAL_HOLLOWS, 20),
        OTHERS(IslandType.NONE),
        ;

        val personalCap: Int = personalCap ?: GLOBAL_CAP
        val hasPersonalCap: Boolean = personalCap != null

        companion object {
            fun getForIsland(island: IslandType): FishingCap = entries.find { it.island == island } ?: OTHERS
        }
    }

    private enum class AlertReason(display: String) {
        TIME("Time Alert!"),
        PERSONAL_CAP("Reached Personal Cap!"),
        GLOBAL_CAP("Reached Global Cap!"),
        NO_ALERT("You shouldn't see this, report this as a bug"),
        ;

        val display: String = "§c$display"

        inline val isAlert: Boolean get() = this != NO_ALERT

        fun getColor(reason: AlertReason): String = if (reason == this) "§c" else "§a"
    }

    private var ownMobs: Int = 0
    private var otherMobs: Int = 0
    private val totalMobs: Int get() = ownMobs + otherMobs

    private var currentCap = FishingCap.OTHERS

    private var oldestSeaCreature: SeaCreatureData? = null
    private var oldestTime: SimpleTimeMark = SimpleTimeMark.farPast()

    private var display: String? = null
    private var lastWarning: SimpleTimeMark = SimpleTimeMark.farPast()

    @HandleEvent
    fun onSeaCreatureSpawn(event: SeaCreatureEvent.Spawn) = event.seaCreature.handleSpawn()

    @HandleEvent
    fun onSeaCreatureRedetect(event: SeaCreatureEvent.ReDetect) = event.seaCreature.handleSpawn()

    @HandleEvent
    fun onSeaCreatureRemove(event: SeaCreatureEvent.DeSpawn) = event.seaCreature.handleDeSpawn()

    @HandleEvent(onlyOnSkyblock = true)
    fun onSecondPassed() = update()

    @HandleEvent(onlyOnSkyblock = true)
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        config.position.renderString(display, posLabel = "Better Fishing Timer")
    }

    private fun KMutableProperty0<Int>.decrease() = set((get() - 1).coerceAtLeast(0))

    private fun SeaCreatureData.handleSpawn() {
        if (isOwn) ++ownMobs else ++otherMobs
        val oldest = oldestSeaCreature
        if (oldest == null || spawnTime < oldest.spawnTime) {
            oldestSeaCreature = this
            oldestTime = spawnTime
        }
        update()
    }

    private fun SeaCreatureData.handleDeSpawn() {
        val property = if (isOwn) ::ownMobs else ::otherMobs
        property.decrease()
        if (this == oldestSeaCreature) calculateOldest()
        update()
    }

    private fun update() {
        if (totalMobs == 0) {
            if (display != null) reset()
            return
        }
        if (!isEnabled()) return
        val timeSince = oldestTime.passedSince()

        val reason = shouldWarn(timeSince)
        if (reason.isAlert) {
            lastWarning = SimpleTimeMark.now()
            config.sound.playSound()
            TitleManager.sendTitle(reason.display, duration = 2.seconds)
        }

        val timeColor = AlertReason.TIME.getColor(reason)
        val personalCapColor = AlertReason.PERSONAL_CAP.getColor(reason)
        val globalCapColor = AlertReason.GLOBAL_CAP.getColor(reason)

        val formatTime = timeSince.format(showMilliSeconds = false)

        display = buildString {
            append("$timeColor$formatTime §8(")
            if (currentCap.hasPersonalCap) append("$personalCapColor$ownMobs§7/")
            append("$globalCapColor$totalMobs §bsea creatures§8)")
        }

    }

    private fun shouldWarn(timeSince: Duration): AlertReason {
        with(config) {
            return when {
                lastWarning.passedSince() < warningDelay -> AlertReason.NO_ALERT
                timeAlert && timeSince >= timeAlertSeconds.seconds -> AlertReason.TIME
                warnPersonalCap && currentCap.hasPersonalCap && ownMobs >= currentCap.personalCap -> AlertReason.PERSONAL_CAP
                warnGlobalCap && totalMobs >= GLOBAL_CAP -> AlertReason.GLOBAL_CAP
                else -> AlertReason.NO_ALERT
            }
        }

    }

    private fun calculateOldest() {
        oldestSeaCreature = SeaCreatureDetectionApi.getSeaCreatures().minByOrNull { it.spawnTime }
        oldestTime = oldestSeaCreature?.spawnTime.orFarPast()
    }

    private fun reset() {
        ownMobs = 0
        otherMobs = 0
        oldestSeaCreature = null
        oldestTime = SimpleTimeMark.farPast()
        display = null
        lastWarning = SimpleTimeMark.farPast()
    }

    @HandleEvent
    fun onDebug(event: NautilusDebugEvent) {
        event.title("Better Fishing Timer")
        event.addIrrelevant(
            "ownMobs" to ownMobs,
            "otherMobs" to otherMobs,
            "oldestSeaCreature" to oldestSeaCreature,
            "oldestTime" to oldestTime,
            "lastWarning.passedSince()" to lastWarning.passedSince(),
            "display" to display,
        )
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        currentCap = FishingCap.getForIsland(event.newIsland)
        reset()
    }

    private fun isEnabled() = config.enabled

}
