package com.github.itsempa.nautilus.data.fishingevents

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.hypixel.HypixelJoinEvent
import at.hannibal2.skyhanni.utils.ClipboardUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.github.itsempa.nautilus.events.MayorDataUpdateEvent
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.NautilusNullableUtils.orFalse
import com.github.itsempa.nautilus.utils.TimePeriod
import com.github.itsempa.nautilus.utils.TimePeriod.Companion.getCurrentOrNext
import com.github.itsempa.nautilus.utils.minBy
import kotlin.time.Duration

abstract class FishingEvent(val internalName: String) {
    abstract val name: String
    abstract val duration: Duration

    var isActive: Boolean = false
        private set

    /** Current event's time period if active, or next event's */
    var timePeriod: TimePeriod? = null
        private set

    val startTime: SimpleTimeMark get() = timePeriod?.start ?: SimpleTimeMark.farFuture()
    val endTime: SimpleTimeMark get() = timePeriod?.end ?: SimpleTimeMark.farPast()

    private var overrideTimePeriods = emptyList<TimePeriod>()
    private var nextUpdate = SimpleTimeMark.farPast()

    /** Updates the [timePeriod] of the event to be the next event (or null if there isn't a next one). */
    abstract fun updateNextTimePeriod(): TimePeriod?

    init {
        @Suppress("LeakingThis")
        events.add(this)
    }

    private fun onSecondPassed() {
        if (nextUpdate.isInFuture()) return
        val isNow = timePeriod?.isNow().orFalse()
        if (isNow == isActive) return
        isActive = isNow
        if (isNow) onStart()
        else onEnd()
        internalUpdateTimePeriod()
    }

    protected abstract fun onStart()

    protected abstract fun onEnd()

    private fun internalUpdateTimePeriod() {
        val override = overrideTimePeriods.getCurrentOrNext()
        val next = updateNextTimePeriod()
        val newPeriod = when {
            override == null -> next
            next == null -> override
            else -> minBy(next, override, TimePeriod::getNextUpdate)
        }
        val oldActive = isActive

        if (newPeriod != timePeriod) timePeriod = newPeriod

        if (newPeriod != null) {
            nextUpdate = newPeriod.getNextUpdate()

            val nowActive = newPeriod.isNow()
            if (nowActive && !oldActive) {
                isActive = true
                onStart()
            }
        }
    }

    @Module
    companion object {
        private val events = mutableListOf<FishingEvent>()

        // TODO: use repo for override time periods

        @HandleEvent(eventTypes = [HypixelJoinEvent::class, MayorDataUpdateEvent::class])
        fun onForceUpdate() = events.forEach(FishingEvent::internalUpdateTimePeriod)

        @HandleEvent
        fun onSecondPassed(event: SecondPassedEvent) = events.forEach(FishingEvent::onSecondPassed)

        @HandleEvent
        fun onCommandRegistration(event: NautilusCommandRegistrationEvent) {
            event.register("ntfishingevent") {
                this.description = "Gives debug information about upcoming or current events."
                this.callback {
                    val text = events.joinToString("\n") {
                        buildString {
                            appendLine(it.internalName)
                            appendLine(" - timePeriod: ${it.timePeriod?.formatString()}")
                            appendLine(" - nextUpdate: in ${it.nextUpdate.timeUntil()} ")
                            appendLine()
                        }
                    }
                    ClipboardUtils.copyToClipboard(text)
                    NautilusChat.chat("Copied Fishing Event data to clipboard!")
                }
            }
        }
    }
}
