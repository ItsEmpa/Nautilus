package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.modules.Module

@Module(devOnly = true)
object BrigadierTest {

    @HandleEvent
    fun onCommand(event: BrigadierRegisterEvent) {

    }

}
