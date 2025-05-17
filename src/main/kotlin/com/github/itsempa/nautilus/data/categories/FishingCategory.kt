package com.github.itsempa.nautilus.data.categories

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.fishing.FishingBobberInLiquidEvent
import at.hannibal2.skyhanni.features.fishing.FishingApi
import at.hannibal2.skyhanni.features.misc.IslandAreas
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import com.github.itsempa.nautilus.config.NullableStringTypeAdapter
import com.github.itsempa.nautilus.data.CrystalHollowsArea
import com.github.itsempa.nautilus.data.HotspotApi
import com.github.itsempa.nautilus.data.fishingevents.FishingFestivalEvent
import com.github.itsempa.nautilus.data.fishingevents.JerrysWorkshopEvent
import com.github.itsempa.nautilus.data.fishingevents.SpookyFestivalEvent
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusNullableUtils.orTrue
import com.github.itsempa.nautilus.utils.getSealedObjects
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

    fun isMainActive() = this == activeCategory
    fun isExtraActive() = this in extraCategories
    fun isActive() = isMainActive() || isExtraActive()
    fun hasParent() = parent != null

    internal fun checkTreeActive(): Boolean = checkActive() && parent?.checkTreeActive().orTrue()
    internal open fun checkActive(): Boolean = false

    fun getPredecessors(): List<FishingCategory> {
        val parent = parent ?: return emptyList()
        return parent.getPredecessors() + parent
    }

    fun isPredecessor(potentialParent: FishingCategory): Boolean = potentialParent in getPredecessors()

    data object Lava : FishingCategory("LAVA") {
        override fun checkActive(): Boolean = lastWater == false

        data object CrimsonIsle : FishingCategory("CRIMSON_ISLE") {
            override fun checkActive(): Boolean = IslandType.CRIMSON_ISLE.isInIsland()

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
            override fun checkActive(): Boolean = IslandType.CRYSTAL_HOLLOWS.isInIsland()

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
            override fun checkActive(): Boolean = IslandType.CRYSTAL_HOLLOWS.isInIsland()

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
            override fun checkActive(): Boolean = IslandType.DWARVEN_MINES.isInIsland()

            data object Grubbers : FishingCategory("DW_WATER_GRUBBERS") {
                override fun checkActive(): Boolean = true
            }
        }

        // TODO: figure out what to do with this
        data object Park : FishingCategory("PARK") {
            override fun checkActive(): Boolean = IslandType.THE_PARK.isInIsland()

            data object InkFishing : FishingCategory("INK_FISHING") {
                override fun checkActive(): Boolean = IslandAreas.currentAreaName == "Birch Park"
            }
        }

        data object Bayou : FishingCategory("BAYOU") {
            override fun checkActive(): Boolean = IslandType.BACKWATER_BAYOU.isInIsland()
        }

        data object Oasis : FishingCategory("OASIS") {
            override fun checkActive(): Boolean = IslandType.THE_FARMING_ISLANDS.isInIsland()
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
                    return JerrysWorkshopEvent.isActive && IslandType.WINTER.isInIsland()
                }
            }

            data object FishingFestival : FishingCategory("FISHING_FESTIVAL", true) {
                override fun checkActive(): Boolean = FishingFestivalEvent.isActive
            }
        }
    }

    // TODO: create event for when active categories change
    @Module
    companion object {
        val categories: Map<String, FishingCategory>
        private val parentCategories: List<FishingCategory>
        private var lastWater: Boolean? = null
        var activeCategory: FishingCategory? = null
            private set

        var extraCategories: Set<FishingCategory> = emptySet()
            private set

        // TODO: improve active system for categories
        val anyActiveCategories: Set<FishingCategory>
            get() = categories.values.filterTo(mutableSetOf()) { it.checkTreeActive() }

        init {
            val list = getSealedObjects<FishingCategory>()
            val map = mutableMapOf<String, FishingCategory>()
            for (obj in list) {
                check(map.put(obj.internalName, obj) == null) { "${obj.internalName} has a duplicate internal name" }
            }
            categories = map
            parentCategories = list.filter { !it.hasParent() }
        }

        val TYPE_ADAPTER = NullableStringTypeAdapter(
            FishingCategory::internalName,
            ::getCategoryByInternalName,
        )

        fun getCategoryByInternalName(name: String): FishingCategory? = categories[name]

        @HandleEvent
        fun onBobber(event: FishingBobberInLiquidEvent) {
            lastWater = event.onWater
        }

        @HandleEvent
        fun onDebug(event: NautilusDebugEvent) {
            event.title("FishingCategories")
            event.addIrrelevant(getTree())
        }

        @HandleEvent
        fun onSecondPassed() {
            val result = findCategories()
            if (result == null) {
                activeCategory = null
                extraCategories = emptySet()
                return
            }
            activeCategory = result.first
            extraCategories = result.second.toSet()
        }

        private fun findCategories(): Pair<FishingCategory, List<FishingCategory>>? {
            var current = parentCategories.find { it.checkActive() } ?: return null
            val eventCategories = mutableListOf<FishingCategory>()

            while (true) {
                val activeChildren = current.children.filter { it.checkActive() }

                eventCategories.addAll(activeChildren.filter { it.extraCategory })

                val activeChild = activeChildren.find { !it.extraCategory }

                if (activeChild == null) {
                    if (eventCategories.isNotEmpty()) {
                        val filtered = eventCategories.find { it.isPredecessor(current) }
                        if (filtered != null) {
                            return filtered to eventCategories.apply { remove(filtered) }
                        }
                    }
                    return current to eventCategories
                }

                current = activeChild
            }
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
                val suffix = if (category.isMainActive()) "ACTIVE"
                else if (category.isExtraActive()) "EXTRA" else ""
                val tick = if (category.checkActive()) "✔" else "✘"

                result.add("$currentPrefix${category.internalName} - $tick $suffix")
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
