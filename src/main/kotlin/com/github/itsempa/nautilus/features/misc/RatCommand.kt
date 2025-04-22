package com.github.itsempa.nautilus.features.misc

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat

@Module
object RatCommand {

    private val text = """
    rat       _..----.._    _
            .'  .--.    "-.(0)_
'-.__.-'"'=:|   ,  _)_ \__ . c\'-..
             '''------'---''---'-"
    """.trimIndent()

    @HandleEvent
    fun onCommandRegistration(event: NautilusCommandRegistrationEvent) {
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
