package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.StringUtils.equalsIgnoreColor
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat

@Module
object DebugCommand {

    @HandleEvent
    fun onCommand(event: NautilusCommandRegistrationEvent) {
        event.register("ntdebug") {
            this.description = "Copies ${Nautilus.MOD_NAME} debug data in the clipboard."
            this.category = CommandCategory.DEVELOPER_DEBUG
            callback(::debugCommand)
        }
    }

    private fun debugCommand(args: Array<String>) {
        val list = mutableListOf<String>()
        list.add("```")
        list.add("= Debug Information for ${Nautilus.MOD_NAME} ${Nautilus.VERSION} =")
        list.add("")

        val search = args.joinToString(" ")
        list.add(
            if (search.isNotEmpty()) {
                if (search.equalsIgnoreColor("all")) {
                    "search for everything:"
                } else "search '$search':"
            } else "no search specified, only showing interesting stuff:",
        )

        val event = NautilusDebugEvent(list, search)
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
