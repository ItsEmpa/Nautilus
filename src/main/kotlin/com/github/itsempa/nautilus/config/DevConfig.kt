package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.config.dev.RepoConfig
import com.google.gson.annotations.Expose

class DevConfig {

    @Expose
    @ConfigOption(name = "Repository Location", desc = "")
    @Accordion
    val repoLocation: RepoConfig = RepoConfig()
}
