package com.github.itsempa.nautilus.data.categories

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.fishing.FishingBobberInLiquidEvent
import at.hannibal2.skyhanni.features.fishing.FishingApi
import at.hannibal2.skyhanni.utils.ClipboardUtils
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import com.github.itsempa.nautilus.commands.brigadier.BrigadierArguments
import com.github.itsempa.nautilus.config.core.loader.NullableStringTypeAdapter
import com.github.itsempa.nautilus.data.CrystalHollowsArea
import com.github.itsempa.nautilus.data.HotspotApi
import com.github.itsempa.nautilus.data.fishingevents.FishingFestivalEvent
import com.github.itsempa.nautilus.data.fishingevents.JerrysWorkshopEvent
import com.github.itsempa.nautilus.data.fishingevents.SpookyFestivalEvent
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.events.FishingCategoryUpdateEvent
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.NautilusNullableUtils.orTrue
import com.github.itsempa.nautilus.utils.getSealedObjects
import me.owdding.ktmodules.Module
import kotlin.reflect.KClass

@Suppress("unused")
/**
 * The subclasses found here represent a graph/tree of different fishing categories,
 * where the "children" are the nested classes, and for a category to be considered "active",
 * all of its predecessors need to also be considered active.
 */
sealed class FishingCategory(val internalName: String, val extraCategory: Boolean = false) {
    var parent: FishingCategory? = null
        private set
    val children: List<FishingCategory> = getNestedClasses(this::class, this)

    var isActive: Boolean = false
        private set

    /** True only if none of its children are active. */
    var isMainActive: Boolean = false
        private set

    fun hasParent() = parent != null

    private fun updateActive() {
        isActive = checkTreeActive()
    }

    private fun updateMainActive() {
        isMainActive = isActive && children.none { it.isActive }
    }

    private fun getDebug(): String {
        val data = listOf(
            "internalName" to internalName,
            "isActive" to isActive,
            "isMainActive" to isMainActive,
            "extraCategory" to extraCategory,
            "parent" to parent?.internalName,
            "children" to children.map { it.internalName },
        )
        return buildString {
            appendLine("== $internalName ==")
            for ((key, value) in data) {
                appendLine("  $key: $value")
            }
        }
    }

    private fun checkTreeActive(): Boolean = checkActive() && parent?.checkTreeActive().orTrue()
    internal open fun checkActive(): Boolean = false

    fun getPredecessors(): List<FishingCategory> {
        val parent = parent ?: return emptyList()
        return parent.getPredecessors() + parent
    }

    fun isPredecessor(potentialParent: FishingCategory): Boolean = potentialParent in getPredecessors()

    data object Lava : FishingCategory("LAVA") {
        override fun checkActive(): Boolean = lastWater == false

        data object CrimsonIsle : FishingCategory("CRIMSON_ISLE") {
            override fun checkActive(): Boolean = IslandType.CRIMSON_ISLE.isCurrent()

            data object TrophyFish : FishingCategory("TROPHY_FISH") {
                override fun checkActive(): Boolean = FishingApi.wearingTrophyArmor
            }

            data object Fish : FishingCategory("CI_FISH") {
                override fun checkActive(): Boolean = !TrophyFish.checkActive()

                data object Hotspot : FishingCategory("CI_HOTSPOT") {
                    override fun checkActive(): Boolean = HotspotApi.isHotspotFishing()
                }
            }
        }

        data object CrystalHollows : FishingCategory("CH_LAVA") {
            override fun checkActive(): Boolean = IslandType.CRYSTAL_HOLLOWS.isCurrent()

            data object MagmaCore : FishingCategory("MAGMA_CORE") {
                override fun checkActive(): Boolean = CrystalHollowsArea.MAGMA_FIELDS.inArea()
            }

            data object Worms : FishingCategory("CH_LAVA_WORMS") {
                override fun checkActive(): Boolean = CrystalHollowsArea.PRECURSOR_REMNANTS.inArea()
            }
        }
    }

    data object Water : FishingCategory("WATER") {
        override fun checkActive(): Boolean = lastWater == true

        data object CrystalHollows : FishingCategory("CH_WATER") {
            override fun checkActive(): Boolean = IslandType.CRYSTAL_HOLLOWS.isCurrent()

            data object Worm : FishingCategory("CH_WATER_WORM") {
                override fun checkActive(): Boolean = CrystalHollowsArea.GOBLIN_HOLDOUT.inArea()
            }

            data object AbyssalMines : FishingCategory("ABYSSAL_MINES") {
                override fun checkActive(): Boolean = CrystalHollowsArea.inAnyArea(
                    CrystalHollowsArea.JUNGLE,
                    CrystalHollowsArea.PRECURSOR_REMNANTS,
                    CrystalHollowsArea.MITHRIL_DEPOSITS,
                )
            }
        }

        data object DwarvenMines : FishingCategory("DW_WATER") {
            override fun checkActive(): Boolean = IslandType.DWARVEN_MINES.isCurrent()

            data object Grubbers : FishingCategory("DW_WATER_GRUBBERS") {
                override fun checkActive(): Boolean = true
            }
        }

        // TODO: figure out what to do with this
        data object Park : FishingCategory("PARK") {
            override fun checkActive(): Boolean = IslandType.THE_PARK.isCurrent()

            data object InkFishing : FishingCategory("INK_FISHING") {
                override fun checkActive(): Boolean = SkyBlockUtils.graphArea == "Birch Park"
            }
        }

        data object Bayou : FishingCategory("BAYOU") {
            override fun checkActive(): Boolean = IslandType.BACKWATER_BAYOU.isCurrent()
        }

        data object Oasis : FishingCategory("OASIS") {
            override fun checkActive(): Boolean = IslandType.THE_FARMING_ISLANDS.isCurrent()
        }

        data object Hotspot : FishingCategory("WATER_HOTSPOT", extraCategory = true) {
            override fun checkActive(): Boolean = HotspotApi.isHotspotFishing()
        }

        object Events {
            data object Spooky : FishingCategory("SPOOKY", true) {
                override fun checkActive(): Boolean = SpookyFestivalEvent.isActive
            }

            data object Jerry : FishingCategory("JERRY", true) {
                override fun checkActive(): Boolean {
                    return JerrysWorkshopEvent.isActive && IslandType.WINTER.isCurrent()
                }
            }

            data object FishingFestival : FishingCategory("FISHING_FESTIVAL", true) {
                override fun checkActive(): Boolean = FishingFestivalEvent.isActive
            }
        }
    }

    @Module
    companion object {
        private val categoriesMap: Map<String, FishingCategory>
        val categories: List<FishingCategory>
        val parentCategories: List<FishingCategory>

        private var lastWater: Boolean? = null

        var activeCategories: Set<FishingCategory> = emptySet()
            private set

        init {
            val list = getSealedObjects<FishingCategory>()
            val map = mutableMapOf<String, FishingCategory>()
            for (obj in list) {
                check(map.put(obj.internalName, obj) == null) { "${obj.internalName} has a duplicate internal name" }
            }
            categoriesMap = map
            categories = list
            parentCategories = list.filter { !it.hasParent() }
        }

        val TYPE_ADAPTER = NullableStringTypeAdapter(
            FishingCategory::internalName,
            ::getCategoryByInternalName,
        )

        fun getCategoryByInternalName(name: String): FishingCategory? = categoriesMap[name]

        @HandleEvent
        fun onBobber(event: FishingBobberInLiquidEvent) {
            lastWater = event.onWater
        }

        @HandleEvent
        fun onCommand(event: BrigadierRegisterEvent) {
            event.register("ntfishingcategories") {
                this.category = CommandCategory.DEVELOPER_DEBUG
                this.description = "Shows information about all fishing categories."

                argCallback("name", BrigadierArguments.simpleMap(categoriesMap)) { category ->
                    val debug = category.getDebug()
                    ClipboardUtils.copyToClipboard(debug)
                    NautilusChat.chat("Copied debug information of category '${category.internalName}' to clipboard.")
                }

                literalCallback("all") {
                    val debugInfo = categories.joinToString("\n\n\n") { it.getDebug() }
                    ClipboardUtils.copyToClipboard(debugInfo)
                    NautilusChat.chat("Copied debug information of all fishing categories to clipboard.")
                }

                literalCallback("tree") {
                    val tree = getTree().joinToString("\n")
                    ClipboardUtils.copyToClipboard(tree)
                    NautilusChat.chat("Copied the fishing categories tree to clipboard.")
                }
            }
        }

        @HandleEvent
        fun onSecondPassed() {
            val oldCategories = activeCategories
            activeCategories = findCategories()
            if (oldCategories == activeCategories) return
            FishingCategoryUpdateEvent(activeCategories, oldCategories).post()
        }

        private fun findCategories(): Set<FishingCategory> {
            categories.forEach { it.updateActive() }
            categories.forEach { it.updateMainActive() }
            return categories.filterTo(mutableSetOf()) { it.isActive }
        }

        private fun getNestedClasses(clazz: KClass<*>, parent: FishingCategory): List<FishingCategory> {
            val list = mutableListOf<FishingCategory>()
            for (nested in clazz.nestedClasses) {
                val nestedInstance = nested.objectInstance ?: continue
                if (nestedInstance !is FishingCategory) {
                    list.addAll(getNestedClasses(nested, parent))
                    continue
                }
                nestedInstance.parent = parent
                list.add(nestedInstance)
            }
            return list
        }

        // TODO: maybe slightly change the function to make it more compact
        private fun getTree(): List<String> {
            val result = mutableListOf<String>()
            fun getPrefixes(childrenPrefix: String, currentIndex: Int, lastIndex: Int): Pair<String, String> {
                return if (currentIndex == lastIndex) "$childrenPrefix└" to "$childrenPrefix "
                else "$childrenPrefix├" to "${childrenPrefix}│"
            }

            fun printCategory(category: FishingCategory, currentPrefix: String, childrenPrefix: String) {
                val suffix = if (category.isMainActive) " MAIN"
                else ""
                val tick = if (category.isActive) "✔" else "✘"

                result.add("$currentPrefix${category.internalName} - $tick$suffix")
                val children = category.children
                for ((index, child) in children.withIndex()) {
                    val (parentPrefix, nextPrefix) = getPrefixes(childrenPrefix, index, children.lastIndex)
                    printCategory(child, parentPrefix, nextPrefix)
                }
            }

            for ((index, parent) in parentCategories.withIndex()) {
                val (thisPrefix, nextPrefix) = getPrefixes("", index, parentCategories.lastIndex)
                printCategory(parent, thisPrefix, nextPrefix)
            }
            return result
        }
    }
}
