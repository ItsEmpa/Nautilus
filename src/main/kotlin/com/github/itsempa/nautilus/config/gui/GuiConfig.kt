package com.github.itsempa.nautilus.config.gui

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigLink
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.google.gson.annotations.Expose

class GuiConfig {

    @Expose
    @ConfigOption(name = "Camera Move Warning", desc = "")
    @Accordion
    val cameraMove: CameraMoveConfig = CameraMoveConfig()

    @Expose
    @ConfigOption(name = "Health Display", desc = "")
    @Accordion
    val healthDisplay = HealthDisplayConfig()

    @Expose
    @ConfigOption(name = "Legion & Bobbin' Time Display", desc = "")
    @Accordion
    val legionBobbinDisplay: LegionBobbinConfig = LegionBobbinConfig()

    @Expose
    @ConfigOption(name = "Rain Timer", desc = "")
    @Accordion
    val rainTimer: RainTimerConfig = RainTimerConfig()

    @Expose
    @ConfigOption(name = "Rare Drops Tracker", desc = "")
    @Accordion
    val rareDropsTracker: RareDropsTrackerConfig = RareDropsTrackerConfig()

    @Expose
    @ConfigOption(name = "Spooky Counter", desc = "Shows the amount of mobs you have fished in the current spooky festival.")
    @ConfigEditorBoolean
    var spookyCounter: Boolean = true

    @Expose
    @ConfigLink(owner = GuiConfig::class, field = "spookyCounter")
    val spookyCounterPos = Position(-300, 100)

}
