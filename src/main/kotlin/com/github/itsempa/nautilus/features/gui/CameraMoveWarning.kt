package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.TitleManager
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.events.FishCatchEvent
import me.owdding.ktmodules.Module

@Module
object CameraMoveWarning {

    private val config get() = Nautilus.feature.gui.cameraMove

    @HandleEvent
    fun onCatch(event: FishCatchEvent) {
        if (!config.enabled) return
        if (event.sinceMove >= config.catches) {
            TitleManager.sendTitle("§cMove camera!", "Or you won't catch any feeshes!")
            config.sound.playSound()
        }
    }

}
