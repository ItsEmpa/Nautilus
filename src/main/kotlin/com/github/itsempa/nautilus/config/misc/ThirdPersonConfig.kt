package com.github.itsempa.nautilus.config.misc

import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorSlider
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class ThirdPersonConfig {

    @Expose
    @ConfigOption(name = "Third Person Fov", desc = "Use a different Fov while in third person.")
    @ConfigEditorBoolean
    var thirdPersonFov: Boolean = false

    @Expose
    @ConfigOption(name = "Fov", desc = "The Fov to use when in third person.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 160f, minStep = 1f)
    var fov: Float = 110f

    @Expose
    @ConfigOption(name = "Remove Front Camera", desc = "Removes the front camera.")
    @ConfigEditorBoolean
    var removeFrontCamera: Boolean = false
}
