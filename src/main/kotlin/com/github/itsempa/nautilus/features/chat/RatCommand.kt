package com.github.itsempa.nautilus.features.chat

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.utils.NautilusChat
import me.owdding.ktmodules.Module

@Module
object RatCommand {

    private val text = """
    rat       _..----.._    _
            .'  .--.    "-.(0)_
'-.__.-'"'=:|   ,  _)_ \__ . c\'-..
             '''------'---''---'-"
    """.trimIndent()

    @HandleEvent
    fun onCommandRegistration(event: BrigadierRegisterEvent) {
        event.register("nautilusrat") {
            this.aliases = listOf("ntrat", "rat")
            this.description = "BIG SCARY RAT!!!"
            this.category = CommandCategory.DEVELOPER_TEST
            callback {
                NautilusChat.chat(text, prefix = false)
            }
        }
    }


}
