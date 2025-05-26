package com.github.itsempa.nautilus.utils.tracker

import com.github.itsempa.nautilus.data.categories.FishingCategory
import com.google.gson.annotations.Expose

@Suppress("unused")
class FishingCategoryTrackerData<Data : Addable<Data>>(
    private val newData: () -> Data,
) : NautilusTrackerData() {

    @Expose
    private val categories = mutableMapOf<FishingCategory, Data>()

    override fun reset() = categories.clear()

    fun modify(category: FishingCategory, block: (Data) -> Unit) = block(get(category))

    fun get(category: FishingCategory): Data = categories.getOrPut(category, newData)

    fun flatten(): Data {
        return if (categories.isEmpty()) newData()
        else categories.values.reduce(Addable<Data>::plus)
    }
}
