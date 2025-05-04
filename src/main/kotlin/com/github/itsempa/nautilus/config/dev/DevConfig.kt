package com.github.itsempa.nautilus.config.dev

import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class DevConfig {

    @Expose
    @ConfigOption(name = "Highlight Sea Creatures", desc = "Show a highlight on your own sea creatures as pink, and as blue for other's.")
    @ConfigEditorBoolean
    var highlightSeaCreatures: Boolean = false

    @Expose
    @ConfigOption(name = "Repository Location", desc = "")
    @Accordion
    val repoLocation: RepoConfig = RepoConfig()
}
