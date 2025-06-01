package com.github.itsempa.nautilus.config.render

import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorSlider
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class HotspotHighlightConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Highlight hotspots..")
    @ConfigEditorBoolean
    var enabled: Boolean = true

    @Expose
    @ConfigOption(name = "Hide Particles", desc = "Hides the particles created by hotspots..")
    @ConfigEditorBoolean
    var hideParticles: Boolean = true

    @Expose
    @ConfigOption(name = "Transparency", desc = "Changes the transparency of the hotspot highlight (255 being fully opaque, 0 being fully transparent).")
    @ConfigEditorSlider(minValue = 0f, maxValue = 255f, minStep = 1f)
    var transparency: Int = 127

}
