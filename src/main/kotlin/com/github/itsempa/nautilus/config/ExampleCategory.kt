package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigLink
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class ExampleCategory {
    @Expose
    @ConfigOption(name = "Example Option", desc = "This is an example option.")
    @ConfigEditorBoolean
    var exampleOption: Boolean = false

    @Expose
    @ConfigLink(owner = ExampleCategory::class, field = "exampleOption")
    val position: Position = Position(20, 20)
}
