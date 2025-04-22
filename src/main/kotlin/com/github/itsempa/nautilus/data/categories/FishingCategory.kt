package com.github.itsempa.nautilus.data.categories

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.fishing.FishingBobberInLiquidEvent
import at.hannibal2.skyhanni.features.fishing.FishingApi
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.addNotNull
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.modules.Module
import kotlin.reflect.KClass

@Suppress("unused")
sealed class FishingCategory(val internalName: String, private val canDuplicate: Boolean = false) {
    private var parent: FishingCategory? = null
    private val children: MutableList<FishingCategory> = mutableListOf()

    fun isActive() = (parent?.checkActive() ?: true) && checkActive()
    internal open fun checkActive(): Boolean = false

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

            data object MagmaCore : FishingCategory("MAGMA_CORE")

            data object Worms : FishingCategory("CH_LAVA_WORMS")
        }
    }

    data object Water : FishingCategory("WATER") {
        override fun checkActive(): Boolean = lastWater == true

        data object CrystalHollows : FishingCategory("CH_WATER") {
            override fun checkActive(): Boolean = IslandType.CRYSTAL_HOLLOWS.isInIsland()

            data object Worm : FishingCategory("CH_WATER_WORM")

            data object AbyssalMines : FishingCategory("ABYSSAL_MINES")
        }

        data object DwarvenMines : FishingCategory("DW_WATER") {
            override fun checkActive(): Boolean = IslandType.DWARVEN_MINES.isInIsland()

            data object Grubbers : FishingCategory("DW_WATER_GRUBBERS")
        }

        data object Park : FishingCategory("PARK") {
            override fun checkActive(): Boolean = IslandType.THE_PARK.isInIsland()

            data object InkFishing : FishingCategory("INK_FISHING")
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
                override fun checkActive(): Boolean = false // TODO: Implement Fishing Festival detection
            }
        }
    }

    @Module
    companion object {
        val categories: List<FishingCategory>
        private val extraCategories: List<FishingCategory>
        private val parentCategories: List<FishingCategory>
        private var lastWater: Boolean? = null
        var activeCategory: FishingCategory? = null
            private set

        // TODO: Implement having multiple categories at once
        var activeCategories: Set<FishingCategory> = emptySet()
            private set

        init {
            categories = buildList {
                recursiveLoad(Lava::class)
                recursiveLoad(Water::class)
            }
            parentCategories = categories.filter { it.parent == null }
            extraCategories = categories.filter { it.canDuplicate }
        }

        fun getCategoryByName(name: String): FishingCategory? = categories.find { it.internalName == name }

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
            var currentCategory = parentCategories.find { it.isActive() }
            if (currentCategory == null) {
                activeCategory = null
                return
            }

            while (true) {
                val activeChild = currentCategory!!.children.find { it.checkActive() }
                if (activeChild != null) currentCategory = activeChild
                else break
            }

            activeCategory = currentCategory
        }

        private fun MutableList<FishingCategory>.recursiveLoad(
            clazz: KClass<*>,
            parent: FishingCategory? = null,
        ): FishingCategory? {
            val newParent = clazz.objectInstance as? FishingCategory
            addNotNull(newParent)
            newParent?.parent = parent
            clazz.nestedClasses.forEach {
                val child = recursiveLoad(it, newParent)
                newParent?.children?.addNotNull(child)
            }
            return newParent
        }


        private fun printTree() {
            fun printCategory(category: FishingCategory, level: Int) {
                val prefix = "  ".repeat(level)
                ChatUtils.debug("$prefix${category.internalName}")
                for (child in category.children) {
                    printCategory(child, level + 1)
                }
            }

            for (category in categories) {
                if (category.parent == null) {
                    printCategory(category, 0)
                }
            }
        }
    }
}
