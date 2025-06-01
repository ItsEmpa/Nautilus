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
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SizeLimitedSet
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.addAll
import at.hannibal2.skyhanni.utils.renderables.Renderable
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.FeeshApi
import com.github.itsempa.nautilus.data.RareDropStat
import com.github.itsempa.nautilus.data.core.NautilusStorage
import com.github.itsempa.nautilus.data.repo.FishingCategoriesMobs
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.events.RareDropEvent
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.features.render.LootshareRange
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.NautilusTimeUtils.customFormat
import com.github.itsempa.nautilus.utils.enumSetOf
import com.github.itsempa.nautilus.utils.removeFirstMatches
import com.google.gson.annotations.Expose
import me.owdding.ktmodules.Module
import kotlin.time.Duration.Companion.seconds

@Suppress("UnstableApiUsage")
@Module
object RareDropsTracker {

    private val storage get() = NautilusStorage.profile.seaCreaturesSinceDrops
    private val config get() = Nautilus.feature.gui.rareDropsTracker

    data class RareDropEntry(
        @Expose var count: Int = 0,
        @Expose var seaCreaturesSince: Int = 0,
        @Expose var lastDrop: SimpleTimeMark = SimpleTimeMark.farPast(),
        @Expose var totalSeacreaturesCaught: Int = 0,
        @Expose var totalMagicFind: Long = 0,
        @Expose var lastMagicFind: Int? = null,
    ) {
        fun addSeaCreature(doubleHook: Boolean) {
            if (doubleHook) seaCreaturesSince += 2 else ++seaCreaturesSince
        }

        fun getAverageCreatures(): Double? =
            if (!hasDropped) null else (totalSeacreaturesCaught.toDouble() / count)

        fun getAverageMagicFind(): Double? =
            if (!hasDropped) null else (totalMagicFind.toDouble() / count)

        fun onDrop(magicFind: Int?) {
            totalSeacreaturesCaught += seaCreaturesSince
            ++count
            seaCreaturesSince = 0
            lastDrop = SimpleTimeMark.now()
            if (magicFind != null) totalMagicFind += magicFind
        }

        fun reset() {
            count = 0
            seaCreaturesSince = 0
            lastDrop = SimpleTimeMark.farPast()
            totalSeacreaturesCaught = 0
        }

        inline val hasDropped: Boolean get() = count != 0
    }

    enum class FishingRareDrop(
        val mobName: String,
        internalName: String? = null,
        pluralMobName: String? = null,
        displayMobName: String? = null,
        val checkChat: Boolean = true,
        private val itemDisplayName: String? = null,
    ) {
        SCUTTLER_SHELL("Fiery Scuttler"),
        FLASH(
            "Thunder",
            "ULTIMATE_FLASH;1",
            checkChat = false,
            itemDisplayName = "§dFlash",
        ),
        RADIOACTIVE_VIAL(
            "Lord Jawbus",
            pluralMobName = "Jawbusses",
            displayMobName = "Jawbus",
            itemDisplayName = "§dVial",
        ),
        BURNT_TEXTS("Ragnarok", checkChat = false),
        TIKI_MASK("Wiki Tiki"),
        TITANOBOA_SHED("Titanoboa"),
        EPIC_BABY_YETI("Yeti", "BABY_YETI;3", itemDisplayName = "§5Baby Yeti"),
        LEG_BABY_YETI("Yeti", "BABY_YETI;4", itemDisplayName = "§6Baby Yeti"),
        ;

        val internalName = (internalName ?: name).toInternalName()
        val itemName: String get() = itemDisplayName ?: internalName.repoItemName
        val pluralDisplayMobName: String = pluralMobName ?: "${displayMobName}s"
        val displayMobName = displayMobName ?: mobName
        val entry: RareDropEntry
            get() = storage.getOrPut(internalName, ::RareDropEntry)

        fun isActive(): Boolean = FishingCategoriesMobs.getCategoryOfMob(mobName)?.isActive ?: false

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
    private val recentDeaths = mutableListOf<Pair<FishingRareDrop, SimpleTimeMark>>()
    private val recentDroppedItems = mutableListOf<Pair<FishingRareDrop, SimpleTimeMark>>()

    private val debugRecentDeaths = SizeLimitedSet<Pair<FishingRareDrop, SimpleTimeMark>>(10)
    private val debugDroppedItems = SizeLimitedSet<Pair<FishingRareDrop, SimpleTimeMark>>(10)

    private var activeDrops = enumSetOf<FishingRareDrop>()
    private var renderables = emptyList<Renderable>()

    @HandleEvent
    fun onSeaCreatureDeath(event: SeaCreatureEvent.Death) {
        val data = event.seaCreature
        val drop = FishingRareDrop.mobsToCheck[data.name] ?: return
        val canActuallyGetDrops = event.isOwn || (event.seenDeath && LootshareRange.isInRange(data.actualLastPos))
        if (!canActuallyGetDrops) return
        val pair = drop to SimpleTimeMark.now()
        recentDeaths.add(pair)
        debugRecentDeaths.add(pair)
        handleMobDrop()
    }

    @HandleEvent
    fun onRareDrop(event: RareDropEvent) {
        val drop = FishingRareDrop.chatDrops[event.internalName] ?: return
        val mf = if (event.dropStat == RareDropStat.MAGIC_FIND) event.magicFind else null
        sendMessage(drop, mf)
    }

    @HandleEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (event.source != ItemAddManager.Source.ITEM_ADD) return
        val drop = FishingRareDrop.mobDeathDropsToCheck[event.internalName] ?: return
        val pair = drop to SimpleTimeMark.now()
        recentDroppedItems.add(pair)
        debugDroppedItems.add(pair)

        handleMobDrop()
    }

    private fun handleMobDrop() {
        if (recentDeaths.isEmpty() || recentDroppedItems.isEmpty()) return
        with(recentDroppedItems.iterator()) {
            while (hasNext()) {
                val (drop, _) = next()
                val matchingDrop = recentDeaths.removeFirstMatches { it.first == drop }
                if (matchingDrop != null) {
                    sendMessage(drop, null)
                    remove()
                }
            }
        }
    }


    @HandleEvent(onlyOnSkyblock = true)
    fun onSecondPassed(event: SecondPassedEvent) {
        if (event.repeatSeconds(5)) {
            activeDrops = FishingRareDrop.entries.filterTo(enumSetOf()) { it.isActive() }
        }
        if (config.enabled) updateDisplay()
        if (recentDroppedItems.isNotEmpty()) recentDroppedItems.removeIf { it.second.passedSince() > MAX_TIME }
        if (recentDeaths.isNotEmpty()) recentDeaths.removeIf { it.second.passedSince() > MAX_TIME }
    }

    private fun sendMessage(drop: FishingRareDrop, magicFind: Int?) {
        val entry = drop.entry
        val (dropCount, creatureCount, lastDrop) = entry
        val newDropCount = dropCount + 1
        val pluralized = StringUtils.pluralize(creatureCount, drop.displayMobName, drop.pluralDisplayMobName)
        val message = buildString {
            append(
                "Dropped §6§l$newDropCount${newDropCount.ordinal()} §r${drop.itemName} " +
                    "§3after §b${creatureCount.addSeparators()} §3$pluralized",
            )
            if (entry.hasDropped) append(
                " §3(Last drop was ${lastDrop.passedSince().customFormat(showDeciseconds = false, maxUnits = 3)} ago)",
            )
            if (magicFind != null) append(" §b(mf: $magicFind✯)")
        }
        if (config.enabled && config.sendChatMessage) NautilusChat.chat(message)
        entry.onDrop(magicFind)
        updateDisplay()
    }

    @HandleEvent
    fun onWorldChange() {
        recentDroppedItems.clear()
        recentDeaths.clear()
        renderables = emptyList()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!FeeshApi.isFishing || !config.enabled) return

        config.position.renderRenderables(renderables, posLabel = "Rare Drops Tracker")
    }

    private fun FishingRareDrop.getDisplay(): Renderable {
        val entry = entry
        val seaCreaturesSince = entry.seaCreaturesSince
        val averageSeaCreatures = entry.getAverageCreatures()?.roundTo(2)?.addSeparators()
        val message = buildString {
            append("§c$displayMobName §7since $itemName§7: §b${seaCreaturesSince}")
            averageSeaCreatures?.let {
                append(" §e($it avg)")
            }
            if (entry.hasDropped && config.showTime) {
                append(" §b${entry.lastDrop.passedSince().customFormat(showDeciseconds = false, maxUnits = 2)}")
            }
        }
        val hover = buildList {
            if (averageSeaCreatures != null) {
                add("§7Average §c$pluralDisplayMobName §7since last $itemName: §b$averageSeaCreatures")
            }
            if (entry.hasDropped) {
                add("§7Last drop: §b${entry.lastDrop.passedSince().customFormat(showDeciseconds = false)} ago")
            }
            if (!checkChat) {
                add("§cCan't get magic find from this drop!")
            } else {
                val avgMf = entry.getAverageMagicFind()
                if (avgMf == null) add("§cNo magic find data available!")
                else {
                    val lastMf = entry.lastMagicFind
                    addAll(
                        "§3Average magic find: §b${avgMf.roundTo(2)}",
                        "§3Last magic find: §b$lastMf",
                    )
                }
            }
        }
        return Renderable.hoverTips(message, hover)
    }

    private fun updateDisplay() {
        renderables = activeDrops.map { it.getDisplay() }
    }

    @HandleEvent
    fun onSeaCreature(event: SeaCreatureFishEvent) {
        val drop = FishingRareDrop.allMobs[event.seaCreature.name] ?: return
        drop.entry.addSeaCreature(event.doubleHook)
        updateDisplay()
    }

    @HandleEvent
    fun onDebug(event: NautilusDebugEvent) {
        event.title("Rare Drops Tracker")
        event.addIrrelevant(
            "recentDeaths" to recentDeaths,
            "recentDroppedItems" to recentDroppedItems,
            "activeDrops" to activeDrops,
            "debugRecentDeaths" to debugRecentDeaths,
            "debugDroppedItems" to debugDroppedItems,
        )
    }
}
