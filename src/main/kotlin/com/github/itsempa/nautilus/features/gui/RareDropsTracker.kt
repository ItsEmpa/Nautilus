package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.utils.ItemUtils.repoItemName
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.ordinal
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.FeeshApi
import com.github.itsempa.nautilus.data.NautilusStorage
import com.github.itsempa.nautilus.data.categories.FishingCategory
import com.github.itsempa.nautilus.data.repo.FishingCategoriesMobs.getMobs
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.events.RareDropEvent
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.features.render.LootshareRange
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.NautilusTimeUtils.customFormat
import com.github.itsempa.nautilus.utils.enumMapOf
import com.github.itsempa.nautilus.utils.enumSetOf
import com.github.itsempa.nautilus.utils.removeMaxTime
import com.google.gson.annotations.Expose
import kotlin.time.Duration.Companion.seconds

@Module
object RareDropsTracker {

    private val storage get() = NautilusStorage.profile.seaCreaturesSinceDrops
    private val config get() = Nautilus.feature.gui.rareDropsTracker

    data class RareDropEntry(
        @Expose var count: Int = 0,
        @Expose var seaCreaturesSince: Int = 0,
        @Expose var lastDrop: SimpleTimeMark = SimpleTimeMark.farPast(),
    ) {
        fun addSeaCreature(doubleHook: Boolean) {
            if (doubleHook) seaCreaturesSince += 2 else ++seaCreaturesSince
        }

        fun onDrop() {
            ++count
            seaCreaturesSince = 0
            lastDrop = SimpleTimeMark.now()
        }

        fun reset() {
            count = 0
            seaCreaturesSince = 0
            lastDrop = SimpleTimeMark.farPast()
        }

        inline val hasDropped: Boolean get() = count != 0
    }

    enum class FishingRareDrop(
        val mobName: String,
        internalName: String? = null,
        val pluralMobName: String? = null,
        val checkChat: Boolean = true,
    ) {
        SCUTTLER_SHELL("Fiery Scuttler"),
        FLASH("Thunder", "ULTIMATE_FLASH;1", checkChat = false),
        RADIOACTIVE_VIAL("Lord Jawbus", pluralMobName = "Lord Jawbusses"),
        BURNT_TEXTS("Ragnarok", checkChat = false),
        TIKI_MASK("Wiki Tiki"),
        TITANOBOA_SHED("Titanoboa"),
        EPIC_BABY_YETI("Yeti", "BABY_YETI;3"),
        LEG_BABY_YETI("Yeti", "BABY_YETI;4"),
        ;

        val internalName = (internalName ?: name).toInternalName()
        val entry: RareDropEntry
            get() = storage.getOrPut(internalName, ::RareDropEntry)

        fun isActive(): Boolean = FishingCategory.anyActiveCategories.any { mobName in it.getMobs() }

        companion object {
            val mobsToCheck: Map<String, FishingRareDrop>
            val mobDeathDropsToCheck: Map<NeuInternalName, FishingRareDrop>
            val chatDrops: Map<NeuInternalName, FishingRareDrop>
            val allMobs: Map<String, FishingRareDrop>

            init {
                fun <T : Any> getData(onlyFromChat: Boolean, getter: FishingRareDrop.() -> T): Map<T, FishingRareDrop> =
                    entries.filter { it.checkChat == onlyFromChat }.associateBy(getter)

                mobsToCheck = getData(false) { mobName }
                mobDeathDropsToCheck = getData(false) { internalName }
                chatDrops = getData(true) { internalName }
                allMobs = entries.associateBy { it.mobName }
            }
        }
    }

    private val MAX_TIME = 15.seconds
    private val recentDeaths = enumMapOf<FishingRareDrop, SimpleTimeMark>()
    private val recentDroppedItems = enumMapOf<FishingRareDrop, SimpleTimeMark>()

    private var activeDrops = enumSetOf<FishingRareDrop>()

    @HandleEvent
    fun onSeaCreatureDeath(event: SeaCreatureEvent.Death) {
        val data = event.seaCreature
        val drop = FishingRareDrop.mobsToCheck[data.name] ?: return
        val canActuallyGetDrops = event.isOwn || (event.seenDeath && LootshareRange.isInRange(data.actualLastPos))
        if (!canActuallyGetDrops) return
        recentDeaths[drop] = SimpleTimeMark.now()
        handleMobDrop()
    }

    @HandleEvent
    fun onRareDrop(event: RareDropEvent) {
        val drop = FishingRareDrop.chatDrops[event.internalName] ?: return
        sendMessage(drop)
    }

    @HandleEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (event.source != ItemAddManager.Source.ITEM_ADD) return
        val drop = FishingRareDrop.mobDeathDropsToCheck[event.internalName] ?: return
        recentDroppedItems[drop] = SimpleTimeMark.now()
        handleMobDrop()
    }

    private fun handleMobDrop() {
        if (recentDeaths.isEmpty() || recentDroppedItems.isEmpty()) return
        val intersect = recentDroppedItems.keys.intersect(recentDeaths.keys)
        for (drop in intersect) {
            sendMessage(drop)
            recentDroppedItems.remove(drop)
            recentDeaths.remove(drop)
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onSecondPassed(event: SecondPassedEvent) {
        if (event.repeatSeconds(5)) {
            activeDrops = FishingRareDrop.entries.filterTo(enumSetOf()) { it.isActive() }
        }
        if (recentDroppedItems.isNotEmpty()) recentDroppedItems.removeMaxTime(MAX_TIME)
        if (recentDeaths.isNotEmpty()) recentDeaths.removeMaxTime(MAX_TIME)
    }

    private fun sendMessage(drop: FishingRareDrop) {
        val internalName = drop.internalName
        val entry = drop.entry
        val (dropCount, creatureCount, lastDrop) = entry
        val pluralized = StringUtils.pluralize(creatureCount, drop.mobName, drop.pluralMobName)
        var message =
            "Dropped ${(dropCount + 1).ordinal()} ${internalName.repoItemName} §3after §b${creatureCount.addSeparators()} §3$pluralized"
        if (entry.hasDropped) message += "(Last drop was ${lastDrop.passedSince().customFormat(showDeciseconds = false, maxUnits = 3)}"
        if (config.enabled && config.sendChatMessage) NautilusChat.chat(message)
        entry.onDrop()
    }

    @HandleEvent
    fun onWorldChange() {
        recentDroppedItems.clear()
        recentDeaths.clear()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!FeeshApi.isFishing || !config.enabled) return
        val strings = activeDrops.map { it.getDisplay() }
        config.position.renderStrings(strings, posLabel = "Rare Drops Tracker")
    }

    private fun FishingRareDrop.getDisplay(): String {
        val (dropCount, creatureCount, lastDrop) = entry
        val pluralized = StringUtils.pluralize(creatureCount, mobName, pluralMobName)
        var message = "${internalName.repoItemName} §7(${dropCount}): §b${creatureCount} §c$pluralized §7since last one"
        if (entry.hasDropped) {
            message += "§b(${lastDrop.passedSince().customFormat(showDeciseconds = false, maxUnits = 2)})"
        }
        return message
    }

    @HandleEvent
    fun onSeaCreature(event: SeaCreatureFishEvent) {
        val drop = FishingRareDrop.allMobs[event.seaCreature.name] ?: return
        drop.entry.addSeaCreature(event.doubleHook)
    }

    @HandleEvent
    fun onCommand(event: NautilusCommandRegistrationEvent) {
        event.register("ntbreak") {
            callback {
                NautilusChat.chat("hi this is a breakpoint")
            }
        }
    }
}
