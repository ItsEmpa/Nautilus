package com.github.itsempa.nautilus.data.categories

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.fishing.FishingBobberInLiquidEvent
import at.hannibal2.skyhanni.features.fishing.FishingApi
import at.hannibal2.skyhanni.features.misc.IslandAreas
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.addNotNull
import com.github.itsempa.nautilus.data.CrystalHollowsArea
import com.github.itsempa.nautilus.data.fishingevents.FishingFestivalEvent
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat
import kotlin.reflect.KClass

//@Suppress("unused")
sealed class FishingCategory(val internalName: String, private val extraEvent: Boolean = false) {
    private var parent: FishingCategory? = null
    private val children: MutableList<FishingCategory> = mutableListOf()

    fun isMainActive() = this == activeCategory
    fun isExtraActive() = this in extraCategories
    fun isActive() = isMainActive() || isExtraActive()

    internal open fun checkActive(): Boolean = false

    private fun isPredecessor(potentialParent: FishingCategory): Boolean {
        var current = parent
        while (current != null) {
            if (current == potentialParent) return true
            current = current.parent
        }
        return false
    }

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
                    override fun checkActive(): Boolean = false // TODO: Implement Fishing in Hotsspot detection
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
                override fun checkActive(): Boolean = CrystalHollowsArea.JUNGLE.inArea()
            }
        }

        data object DwarvenMines : FishingCategory("DW_WATER") {
            override fun checkActive(): Boolean = IslandType.DWARVEN_MINES.isInIsland()

            data object Grubbers : FishingCategory("DW_WATER_GRUBBERS") {
                override fun checkActive(): Boolean = true
            }
        }

        data object Park : FishingCategory("PARK") {
            override fun checkActive(): Boolean = IslandType.THE_PARK.isInIsland()

            data object InkFishing : FishingCategory("INK_FISHING") {
                override fun checkActive(): Boolean = IslandAreas.currentAreaName == "Birch Park"
            }
        }

        data object Hotspot : FishingCategory("HOTSPOT") {
            override fun checkActive(): Boolean = false // TODO: Implement Fishing in Hotspot detection

            data object Bayou : FishingCategory("BAYOU") {
                override fun checkActive(): Boolean = IslandType.BACKWATER_BAYOU.isInIsland()
            }
        }

        data object Oasis : FishingCategory("OASIS") {
            override fun checkActive(): Boolean = IslandType.THE_FARMING_ISLANDS.isInIsland()
        }

        object Events {
            data object Spooky : FishingCategory("SPOOKY", true) {
                override fun checkActive(): Boolean = false // TODO: Implement Spooky Fishing detection
            }

            data object Jerry : FishingCategory("JERRY", true) {
                override fun checkActive(): Boolean = false // TODO: Implement Jerry Fishing detection
            }

            data object FishingFestival : FishingCategory("FISHING_FESTIVAL", true) {
                override fun checkActive(): Boolean = FishingFestivalEvent.isActive
            }
        }
    }

    @Module
    companion object {
        val categories: Map<String, FishingCategory>
        private val parentCategories: List<FishingCategory>
        private var lastWater: Boolean? = null
        var activeCategory: FishingCategory? = null
            private set

        var extraCategories: Set<FishingCategory> = emptySet()
            private set

        init {
            categories = buildMap {
                recursiveLoad(Lava::class)
                recursiveLoad(Water::class)
            }
            parentCategories = categories.values.filter { it.parent == null }
        }

        fun getCategoryByInternalName(name: String): FishingCategory? = categories[name]

        @HandleEvent
        fun onBobber(event: FishingBobberInLiquidEvent) {
            lastWater = event.onWater
        }

        @HandleEvent
        fun onCommand(event: NautilusCommandRegistrationEvent) {
            event.register("ntfishingcategorytest") {
                this.description = "Prints the fishing category tree"
                this.category = CommandCategory.DEVELOPER_TEST
                callback { printTree() }
            }
        }

        @HandleEvent
        fun SecondPassed(event: SecondPassedEvent) {
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

                eventCategories.addAll(activeChildren.filter { it.extraEvent })

                val activeChild = activeChildren.find { !it.extraEvent }

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

        private fun MutableMap<String, FishingCategory>.recursiveLoad(
            clazz: KClass<*>,
            parent: FishingCategory? = null,
        ): FishingCategory? {
            val newParent = clazz.objectInstance as? FishingCategory
            if (newParent != null) {
                put(newParent.internalName, newParent)
                newParent?.parent = parent
            }
            val parentToAssign = newParent ?: parent
            clazz.nestedClasses.forEach {
                val child = recursiveLoad(it, parentToAssign)
                parentToAssign?.children?.addNotNull(child)
            }
            return newParent
        }


        private fun printTree() {
            fun printCategory(category: FishingCategory, level: Int) {
                val prefix = "  ".repeat(level)
                val suffix = if (activeCategory == category) "§cACTIVE"
                else if (category in extraCategories) "§eEXTRA" else ""
                // add tick emoji
                val tick = if (category.checkActive()) "§a✔" else "§7✘"

                NautilusChat.debug("$prefix${category.internalName} - $tick $suffix")
                for (child in category.children) {
                    printCategory(child, level + 1)
                }
            }

            for (category in categories.values) {
                if (category.parent == null) {
                    printCategory(category, 0)
                }
            }
        }
    }
}
