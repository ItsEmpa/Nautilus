package com.github.itsempa.nautilus.utils.tracker

import com.github.itsempa.nautilus.data.core.NullIfEmpty
import com.google.gson.annotations.Expose

data class TrackerStorage<Data : NautilusTrackerData>(
    @Expose
    val total: Data,
    @Expose
    var currentCustomSession: CustomSession<Data>? = null,
    @Expose @NullIfEmpty
    val prevCustomSessions: MutableList<CustomSession<Data>> = mutableListOf(),
    @Expose @NullIfEmpty
    val extraDisplayModes: MutableMap<TrackerDisplayMode, Data> = mutableMapOf(),
)
