package com.github.itsempa.nautilus.data.repo

import at.hannibal2.skyhanni.api.event.HandleEvent
import com.github.itsempa.nautilus.data.categories.FishingCategory
import com.github.itsempa.nautilus.data.repo.json.FishingCategoriesMobsJson
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.events.NautilusRepositoryReloadEvent
import me.owdding.ktmodules.Module

@Module
object FishingCategoriesMobs {

    var categoriesMobData: Map<FishingCategory, List<String>> = emptyMap()
        private set
    private var mobToCategory: Map<String, FishingCategory> = emptyMap()

    @HandleEvent
    fun onRepoLoad(event: NautilusRepositoryReloadEvent) {
        val data = event.getConstant<FishingCategoriesMobsJson>("FishingCategoriesMobs")
        categoriesMobData = data.categories.mapValues { it.value.mobs }
        mobToCategory = categoriesMobData.flatMap { (category, mobs) ->
            mobs.map { mob -> mob to category }
        }.toMap()
    }

    fun getCategoryOfMob(mob: String): FishingCategory? = mobToCategory[mob]
    fun FishingCategory.getMobs(): List<String> = categoriesMobData[this].orEmpty()

    @HandleEvent
    fun onDebug(event: NautilusDebugEvent) {
        event.title("FishingCategoriesMobs")
        event.addIrrelevant(
            "categoriesMobData" to categoriesMobData.entries,
        )
    }

}
