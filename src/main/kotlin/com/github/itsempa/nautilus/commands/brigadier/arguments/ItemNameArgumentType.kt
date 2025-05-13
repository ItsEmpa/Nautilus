package com.github.itsempa.nautilus.commands.brigadier.arguments

import at.hannibal2.skyhanni.utils.CommandContextAwareObject
import at.hannibal2.skyhanni.utils.CommandUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import com.github.itsempa.nautilus.commands.brigadier.BrigadierUtils
import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

class ItemNameArgumentType : ArgumentType<NeuInternalName> {

    private val unknownValueException = DynamicCommandExceptionType { input ->
        LiteralMessage("Unknown item '$input'.")
    }

    private val disallowedValueException = DynamicCommandExceptionType { input ->
        LiteralMessage("Disallowed item '$input'.")
    }

    override fun parse(reader: StringReader): NeuInternalName {
        val input = reader.readString()
        val result = BrigadierUtils.parseItem(reader.readQuotedString())
        return when (result) {
            is NeuInternalName -> result
            BrigadierUtils.ItemParsingFail.DISALLOWED_ITEM -> throw disallowedValueException.createWithContext(reader, input)
            else -> throw unknownValueException.createWithContext(reader, input)
        }
    }

    override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return BrigadierUtils.parseItemNameTabComplete(builder.remainingLowerCase, builder)
    }

    companion object {
        fun itemName(): ItemNameArgumentType = ItemNameArgumentType()
    }
}
