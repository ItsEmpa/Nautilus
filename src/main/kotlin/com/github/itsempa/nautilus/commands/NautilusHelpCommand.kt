package com.github.itsempa.nautilus.commands

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.utils.StringUtils.splitLines
import at.hannibal2.skyhanni.utils.chat.TextHelper
import at.hannibal2.skyhanni.utils.compat.hover
import at.hannibal2.skyhanni.utils.compat.suggest
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.commands.brigadier.BrigadierArguments
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import me.owdding.ktmodules.Module
import net.minecraft.util.IChatComponent

@Module
object NautilusHelpCommand {

    private const val COMMANDS_PER_PAGE = 15
    private val HELP_ID = Nautilus.MOD_ID.hashCode()

    private fun createCommandEntry(command: CommandData): IChatComponent {
        val category = command.category
        val color = category.color
        val description = command.descriptor.splitLines(200).replace("§r", "§7")
        val aliases = command.aliases
        val categoryDescription = category.description.replace("SkyHanni", Nautilus.MOD_NAME).splitLines(200).replace("§r", "§7")

        return TextHelper.text("§7 - $color${command.name}") {
            this.hover = TextHelper.multiline(
                "§3/${command.name}",
                if (aliases.isNotEmpty()) "§3Aliases: §7${command.aliases.joinToString { "/$it" }}" else null,
                if (description.isNotEmpty()) description.prependIndent("  ") else null,
                "",
                "$color§l${category.categoryName}",
                categoryDescription.prependIndent("  "),
            )
            this.suggest = "/${command.name}"
        }
    }

    private fun showPage(commands: List<CommandData>, page: Int = 1, search: String = "") {
        val filtered = commands.filter {
            it.name.contains(search, ignoreCase = true) || it.descriptor.contains(search, ignoreCase = true)
        }

        val title = "${Nautilus.MOD_NAME} Commands" + if (search.isNotBlank()) "Matching: \"$search\"" else ""

        TextHelper.displayPaginatedList(
            title,
            filtered,
            chatLineId = HELP_ID,
            emptyMessage = "No commands found.",
            currentPage = page,
            maxPerPage = COMMANDS_PER_PAGE,
        ) { createCommandEntry(it) }
    }

    @HandleEvent
    fun onCommandRegister(event: BrigadierRegisterEvent) {
        event.register("ntcommands") {
            this.aliases = listOf("nautiluscommands", "nautilushelp", "nthelp")
            this.description = "Shows this list"
            this.category = CommandCategory.MAIN

            arg("-p page", BrigadierArguments.integer(min = 1)) { pageArg ->
                argCallback("search", BrigadierArguments.greedyString()) { search ->
                    val page = get(pageArg)
                    showPage(event.commands, page, search)
                }
                callback {
                    val page = get(pageArg)
                    showPage(event.commands, page)
                }
            }
            argCallback("search", BrigadierArguments.greedyString()) { search ->
                showPage(event.commands, search = search)
            }
            callback {
                showPage(event.commands)
            }
        }
    }
}
