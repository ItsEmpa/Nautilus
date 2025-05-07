package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.utils.OSUtils
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.commands.brigadier.BrigadierArguments
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat

@Module
object DebugCommand {

    @HandleEvent
    fun onCommand(event: BrigadierRegisterEvent) {
        event.register("ntdebug") {
            this.aliases = listOf("nautilusdebug")
            this.description = "Copies ${Nautilus.MOD_NAME} debug data in the clipboard."
            this.category = CommandCategory.DEVELOPER_DEBUG

            literalCallback("all") {
                debugCommand("", true)
            }
            argCallback("search", BrigadierArguments.greedyString()) { search ->
                debugCommand(search, false)
            }
            callback {
                debugCommand("", false)
            }
        }
    }

    private fun debugCommand(search: String, all: Boolean) {
        val list = mutableListOf<String>()
        list.add("```")
        list.add("= Debug Information for ${Nautilus.MOD_NAME} ${Nautilus.VERSION} =")
        list.add("")

        list.add(
            if (all) "search for everything:"
            else if (search.isNotEmpty()) "search '$search':"
            else "no search specified, only showing interesting stuff:",
        )

        val event = NautilusDebugEvent(list, search, all)
        event.post()

        if (event.empty) {
            list.add("")
            list.add("Nothing interesting to show right now!")
            list.add("Looking for something specific? /ntdebug <search>")
            list.add("Wanna see everything? /ntdebug all")
        }

        list.add("```")
        OSUtils.copyToClipboard(list.joinToString("\n"))
        NautilusChat.chat("Copied ${Nautilus.MOD_NAME} debug data in the clipboard.")
    }

}
