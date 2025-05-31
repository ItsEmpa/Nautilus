package com.github.itsempa.nautilus.commands.brigadier.arguments

import at.hannibal2.skyhanni.utils.NeuInternalName
import com.github.itsempa.nautilus.commands.brigadier.BrigadierUtils
import com.github.itsempa.nautilus.commands.brigadier.BrigadierUtils.readOptionalDoubleQuotedString
import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

// TODO: add support for internal name tab completion
sealed class ItemNameArgumentType : ArgumentType<NeuInternalName> {

    private val unknownValueException = DynamicCommandExceptionType { input ->
        LiteralMessage("Unknown item '$input'.")
    }

    private val disallowedValueException = DynamicCommandExceptionType { input ->
        LiteralMessage("Disallowed item '$input'.")
    }

    override fun parse(reader: StringReader): NeuInternalName {
        val input = reader.readOptionalDoubleQuotedString()
        val result = BrigadierUtils.parseItem(input, isValidItem = ::isValidItem)
        return when (result) {
            is NeuInternalName -> result
            BrigadierUtils.ItemParsingFail.DISALLOWED_ITEM -> throw disallowedValueException.createWithContext(reader, input)
            BrigadierUtils.ItemParsingFail.UNKNOWN_ITEM -> throw unknownValueException.createWithContext(reader, input)
            else -> throw IllegalArgumentException("Unexpected item parsing result: $result")
        }
    }

    override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return BrigadierUtils.parseItemNameTabComplete(
            builder.remainingLowerCase,
            builder,
            isValidItem = ::isValidItem
        )
    }

    protected open fun isValidItem(item: NeuInternalName): Boolean = true

    class ItemName(private val isValidItem: (NeuInternalName) -> Boolean = { true }) : ItemNameArgumentType() {
        override fun isValidItem(item: NeuInternalName): Boolean = this.isValidItem.invoke(item)
    }

    companion object {
        fun itemName(
            isValidItem: (NeuInternalName) -> Boolean = { true },
        ): ItemNameArgumentType {
            return ItemName(isValidItem)
        }
    }
}
