package com.github.itsempa.nautilus.data.repo.json

import com.github.itsempa.nautilus.data.categories.FishingCategory
import com.google.gson.annotations.Expose

data class FishingCategoriesMobsJson(
    @Expose val categories: Map<FishingCategory, FishingCategoryMobsDataJson>,
)

data class FishingCategoryMobsDataJson(
    @Expose val mobs: List<String>
)
