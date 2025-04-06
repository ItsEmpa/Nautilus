package com.example.config

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigLink
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption

class ExampleCategory {
    @ConfigOption(name = "Example Option", desc = "This is an example option.")
    @ConfigEditorBoolean
    var exampleOption: Boolean = false

    @ConfigLink(owner = ExampleCategory::class, field = "exampleOption")
    var position: Position = Position(20, 20)
}
