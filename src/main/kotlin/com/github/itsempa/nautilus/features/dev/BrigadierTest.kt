package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import com.github.itsempa.nautilus.commands.brigadier.arguments.LorenzVecArgumentType
import com.github.itsempa.nautilus.commands.brigadier.arguments.LorenzVecArgumentType.Companion.getLorenzVec
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

            thenCallback("restricted pos", LorenzVecArgumentType.int()) {
                val exactLorenzVec = getLorenzVec("pos")
                NautilusChat.chat("Integer Vec: $exactLorenzVec")
            }
            thenCallback("precise pos", LorenzVecArgumentType.double()) {
                val exactLorenzVec = getLorenzVec("pos")
                NautilusChat.chat("Double Vec: $exactLorenzVec")
            }
        }
    }

}
