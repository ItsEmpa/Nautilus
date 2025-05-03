package com.github.itsempa.nautilus.config.gui

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.data.GuiEditManager.openGuiPositionEditor
import at.hannibal2.skyhanni.deps.moulconfig.annotations.Accordion
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorButton
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorKeybind
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigLink
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import at.hannibal2.skyhanni.deps.moulconfig.observer.GetSetter
import at.hannibal2.skyhanni.deps.moulconfig.observer.Property
import com.google.gson.annotations.Expose
import org.lwjgl.input.Keyboard

class GuiConfig {

    @Transient
    @ConfigOption(
        name = "Edit GUI Locations",
        desc = "Opens the Position Editor, allows changing the position of Nautilus' overlays."
    )
    @ConfigEditorButton(buttonText = "Edit")
    private val guiEditor = Runnable { openGuiPositionEditor(true) }

    @Suppress("unused")
    @Transient
    @ConfigOption(name = "Gui Keybind", desc = "Keybind to open the GUI editor.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    private val keybind: Property<Int> = Property.wrap(
        object : GetSetter<Int> {
            override fun get(): Int = SkyHanniMod.feature.gui.keyBindOpen
            override fun set(value: Int) = SkyHanniMod.feature.gui::keyBindOpen.set(value)
        }
    )

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
    @ConfigOption(name = "Hotspot Warning", desc = "")
    @Accordion
    var hotspotWarning: HotspotWarningConfig = HotspotWarningConfig()

    @Expose
    @ConfigOption(name = "Spooky Counter", desc = "Shows the amount of mobs you have fished in the current spooky festival.")
    @ConfigEditorBoolean
    var spookyCounter: Boolean = true

    @Expose
    @ConfigLink(owner = GuiConfig::class, field = "spookyCounter")
    val spookyCounterPos = Position(-300, 100)

    @Expose
    @ConfigOption(name = "Combo Gui", desc = "Shows a simple gui with the current combo and current combo buffs.")
    @ConfigEditorBoolean
    var comboGui: Boolean = false

    @Expose
    @ConfigLink(owner = GuiConfig::class, field = "comboGui")
    val comboGuiPos = Position(-300, 150)

}
