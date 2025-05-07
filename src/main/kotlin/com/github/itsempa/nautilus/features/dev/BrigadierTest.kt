package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import com.github.itsempa.nautilus.commands.brigadier.arguments.LorenzVecArgumentType
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat

@Module(devOnly = true)
object BrigadierTest {

    @HandleEvent
    fun onCommand(event: BrigadierRegisterEvent) {
        event.register("ntbrigadiertest") {
            aliases = listOf("ntbd")
            description = "brigadier test command"

            argCallback("restricted pos", LorenzVecArgumentType.int()) { vec ->
                NautilusChat.chat("Integer Vec: $vec")
            }
            argCallback("precise pos", LorenzVecArgumentType.double()) { vec ->
                NautilusChat.chat("Double Vec: $vec")
            }
        }
    }

}
