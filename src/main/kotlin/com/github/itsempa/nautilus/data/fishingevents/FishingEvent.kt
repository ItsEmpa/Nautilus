package com.github.itsempa.nautilus.data.fishingevents

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.hypixel.HypixelJoinEvent
import at.hannibal2.skyhanni.utils.ClipboardUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.github.itsempa.nautilus.config.NullableStringTypeAdapter
import com.github.itsempa.nautilus.events.FishingEventUpdate
import com.github.itsempa.nautilus.events.MayorDataUpdateEvent
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.NautilusNullableUtils.orFalse
import com.github.itsempa.nautilus.utils.TimePeriod
import com.github.itsempa.nautilus.utils.TimePeriod.Companion.getCurrentOrNext
import com.github.itsempa.nautilus.utils.getSealedObjects
import com.github.itsempa.nautilus.utils.minBy
import kotlin.time.Duration

sealed class FishingEvent(val internalName: String) {
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

    protected open fun onStart() { /* Empty */ }
    protected open fun onEnd() { /* Empty */ }

    protected open fun shouldPostEvents(): Boolean = true

    private fun onSecondPassed() {
        if (nextUpdate.isInFuture()) return
        updateTimePeriodAndState()
    }

    private fun internalStart() {
        onStart()
        NautilusChat.debug("Started Fishing Event $name (${timePeriod?.duration})")
        if (shouldPostEvents()) FishingEventUpdate.Start(this)
    }
    private fun internalEnd() {
        onEnd()
        NautilusChat.debug("Ended Fishing Event $name")
        if (shouldPostEvents()) FishingEventUpdate.End(this)
    }

    private fun updateTimePeriodAndState() {
        val override = overrideTimePeriods.getCurrentOrNext()
        val next = updateNextTimePeriod()
        val newPeriod = when {
            override == null -> next
            next == null -> override
            else -> minBy(next, override, TimePeriod::getNextUpdate)
        }

        if (newPeriod != timePeriod) {
            timePeriod = newPeriod
            nextUpdate = newPeriod?.getNextUpdate() ?: SimpleTimeMark.farPast()
        }

        val nowActive = newPeriod?.isNow().orFalse()
        if (nowActive != isActive) {
            isActive = nowActive
            if (nowActive) internalStart()
            else internalEnd()
        }
    }

    @Module
    companion object {
        val events: List<FishingEvent> = getSealedObjects<FishingEvent>()

        val TYPE_ADAPTER = NullableStringTypeAdapter(
            FishingEvent::internalName,
            ::getByInternalName
        )

        fun getByInternalName(internalName: String): FishingEvent? = events.find { it.name == internalName }

        // TODO: use repo for override time periods

        @HandleEvent(eventTypes = [HypixelJoinEvent::class, MayorDataUpdateEvent::class])
        fun onForceUpdate() = events.forEach(FishingEvent::updateTimePeriodAndState)

        @HandleEvent(SecondPassedEvent::class)
        fun onSecondPassed() = events.forEach(FishingEvent::onSecondPassed)

        @HandleEvent
        fun onCommandRegistration(event: NautilusCommandRegistrationEvent) {
            event.register("ntdebugfishingevents") {
                this.description = "Gives debug information about upcoming or current events."
                this.category = CommandCategory.DEVELOPER_DEBUG
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
