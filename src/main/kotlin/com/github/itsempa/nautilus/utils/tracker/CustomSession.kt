package com.github.itsempa.nautilus.utils.tracker

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.github.itsempa.nautilus.data.core.NullIfEmpty
import com.google.gson.annotations.Expose

data class CustomSession<Data : NautilusTrackerData>(
    @Expose val data: Data,
    @Expose var name: String,
    @Expose val startTime: SimpleTimeMark = SimpleTimeMark.now(),
    @Expose var endTime: SimpleTimeMark? = null,
    @Expose @NullIfEmpty var description: String = ""
) {
    val isActive: Boolean get() = endTime != null
}
