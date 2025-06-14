package com.github.itsempa.nautilus.config.storage

import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.features.misc.update.SemVersion
import com.google.gson.annotations.Expose

class Storage {

    @Expose
    val profiles: MutableMap<String, ProfileStorage> = mutableMapOf()

    // TODO: use this to get changelog between versions on startup
    @Expose
    var lastUsedVersion: SemVersion = Nautilus.SEM_VER

}
