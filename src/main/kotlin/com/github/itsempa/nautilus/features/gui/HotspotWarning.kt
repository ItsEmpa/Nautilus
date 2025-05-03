package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.TitleManager
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.HotspotApi
import com.github.itsempa.nautilus.events.HotspotEvent
import com.github.itsempa.nautilus.modules.Module
import kotlin.time.Duration.Companion.seconds

@Module
object HotspotWarning {

    private val config get() = Nautilus.feature.gui.hotspotWarning

    private var lastHotspot: HotspotApi.Hotspot? = null

    @HandleEvent
    fun onHotspotFish(event: HotspotEvent.StartFishing) {
        lastHotspot = event.hotspot
    }

    @HandleEvent
    fun onHotspotRemoved(event: HotspotEvent.Removed) {
        val hotspot = event.hotspot
        if (hotspot != lastHotspot) return
        lastHotspot = null
        if (HotspotApi.lastHotspotFish.passedSince() > 30.seconds) return
        if (config.enabled) sendWarning()
    }

    private fun sendWarning() {
        TitleManager.sendTitle("§cHotspot disappeared!")
        config.sound.playSound()
    }

}
