package com.github.itsempa.nautilus.config.storage

import com.github.itsempa.nautilus.Nautilus
import com.google.gson.annotations.Expose

class Storage {

    @Expose
    val profileStorage: MutableMap<String, ProfileStorage> = mutableMapOf()

    // TODO: use this to get changelog between versions on startup
    @Expose
    val lastUsedVersion: String = Nautilus.VERSION

}
