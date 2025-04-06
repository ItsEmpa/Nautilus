package com.github.itsempa.nautilus.config.storage

import com.google.gson.annotations.Expose

class Storage {

    @Expose
    val profileStorage: MutableMap<String, ProfileStorage> = mutableMapOf()

}
