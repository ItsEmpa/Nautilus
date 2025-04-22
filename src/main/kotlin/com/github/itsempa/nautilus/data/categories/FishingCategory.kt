package com.github.itsempa.nautilus.data.categories

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.fishing.FishingBobberInLiquidEvent
import at.hannibal2.skyhanni.features.fishing.FishingApi
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.modules.Module

sealed class FishingCategory(val internalName: String, private val parent: FishingCategory? = null) {
    private val children: MutableList<FishingCategory> = mutableListOf()

    fun isActive() = (parent?.checkActive() ?: true) && checkActive()
    internal open fun checkActive(): Boolean = false

    init {
        @Suppress("LeakingThis")
        parent?.children?.add(this)
        @Suppress("LeakingThis")
        categories.add(this)
    }

    @Module
    data object Lava : FishingCategory("LAVA", null) {
        override fun checkActive(): Boolean = lastWater == false

        @Module
        data object CrimsonIsles : FishingCategory("CRIMSON_ISLE", Lava) {
            override fun checkActive(): Boolean = IslandType.CRIMSON_ISLE.isInIsland()

            @Module
            data object TrophyFish : FishingCategory("TROPHY_FISH", CrimsonIsles) {
                override fun checkActive(): Boolean = FishingApi.wearingTrophyArmor
            }

            @Module
            data object CrimsonIsleFish : FishingCategory("CI_FISH", CrimsonIsles) {
                override fun checkActive(): Boolean = !TrophyFish.checkActive()

                @Module
                data object CrimsonIsleHotspot : FishingCategory("CI_HOTSPOT", CrimsonIsles) {
                    override fun checkActive(): Boolean = false // TODO: Implement Fishing in Hotsspot detection
                }
            }
        }

        @Module
        data object CrystalHollows : FishingCategory("CRYSTAL_HOLLOWS", Lava) {
            override fun checkActive(): Boolean = IslandType.CRYSTAL_HOLLOWS.isInIsland()

            @Module
            data object MagmaCore : FishingCategory("MAGMA_CORE", CrystalHollows)

            @Module
            data object Worms : FishingCategory("CH_WORMS", CrystalHollows)
        }
    }

    @Module
    companion object {
        val categories: MutableList<FishingCategory> = mutableListOf()
        val parentCategories: List<FishingCategory> get() = categories.filter { it.parent == null }
        private var lastWater: Boolean? = null
        var activeCategory: FishingCategory? = null
            private set

        fun getCategoryByName(name: String): FishingCategory? = categories.find { it.internalName == name }

        @HandleEvent
        fun onBobber(event: FishingBobberInLiquidEvent) {
            lastWater = event.onWater
        }

        @HandleEvent
        fun onCommand(event: NautilusCommandRegistrationEvent) {
            event.register("ntfishingcategorytest") {
                callback { printTree() }
            }
        }

        @HandleEvent
        fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
            Position(200, 100).renderString("Fishing Category: ${activeCategory?.internalName ?: "None"}", posLabel = "Fishing Category")
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


        fun printTree() {
            fun printCategory(category: FishingCategory, level: Int) {
                val prefix = "  ".repeat(level)
                println("$prefix${category.internalName}")
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
