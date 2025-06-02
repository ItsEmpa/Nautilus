package com.github.itsempa.nautilus.commands.brigadier.arguments

import at.hannibal2.skyhanni.utils.NeuInternalName
import com.github.itsempa.nautilus.commands.brigadier.BrigadierUtils
import com.github.itsempa.nautilus.commands.brigadier.BrigadierUtils.readOptionalDoubleQuotedString
import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

private typealias ParsingFail = BrigadierUtils.ItemParsingFail

// TODO: Maybe add greedy argument support
sealed class InternalNameArgumentType : ArgumentType<NeuInternalName> {

    protected open val showWhenEmpty: Boolean = false

    private val unknownValueException = DynamicCommandExceptionType { input ->
        LiteralMessage("Unknown item '$input'.")
    }

    private val disallowedValueException = DynamicCommandExceptionType { input ->
        LiteralMessage("Disallowed item '$input'.")
    }

    private val emptyValueException = SimpleCommandExceptionType { "Empty item name provided." }

    protected fun parseString(input: String, reader: StringReader): NeuInternalName {
        val result = BrigadierUtils.parseItem(input, isValidItem = ::isValidItem)
        return when (result) {
            is NeuInternalName -> result
            ParsingFail.DISALLOWED_ITEM -> throw disallowedValueException.createWithContext(reader, input)
            ParsingFail.UNKNOWN_ITEM -> throw unknownValueException.createWithContext(reader, input)
            ParsingFail.EMPTY -> throw emptyValueException.createWithContext(reader)
            else -> throw IllegalArgumentException("Unexpected item parsing result: $result")
        }
    }

    protected open fun isValidItem(item: NeuInternalName): Boolean = true

    private open class ItemName : InternalNameArgumentType() {
        override fun parse(reader: StringReader): NeuInternalName {
            val input = reader.readOptionalDoubleQuotedString()
            return parseString(input, reader)
        }

        override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            return BrigadierUtils.parseItemNameTabComplete(
                builder.remainingLowerCase,
                builder,
                showWhenEmpty = showWhenEmpty,
                isValidItem = ::isValidItem,
            )
        }
    }

    private open class InternalName : InternalNameArgumentType() {
        override fun parse(reader: StringReader): NeuInternalName {
            val input = reader.readOptionalDoubleQuotedString()
            return parseString(input, reader)
        }

        override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            return BrigadierUtils.parseInternalNameTabComplete(
                builder.remaining,
                builder,
                showWhenEmpty = showWhenEmpty,
                isValidItem = ::isValidItem,
            )
        }
    }

    @Suppress("unused")
    companion object {
        fun itemName(): InternalNameArgumentType = ItemName()

        fun itemName(isValid: (NeuInternalName) -> Boolean): InternalNameArgumentType {
            return object : ItemName() {
                private val isValid: (NeuInternalName) -> Boolean = isValid
                override fun isValidItem(item: NeuInternalName): Boolean = this.isValid(item)
            }
        }

        fun itemName(showWhenEmpty: Boolean, isValid: (NeuInternalName) -> Boolean): InternalNameArgumentType {
            return object : ItemName() {
                override val showWhenEmpty: Boolean = showWhenEmpty
                private val isValid: (NeuInternalName) -> Boolean = isValid
                override fun isValidItem(item: NeuInternalName): Boolean = this.isValid(item)
            }
        }

        fun itemName(allowed: Collection<NeuInternalName>, showWhenEmpty: Boolean = false): InternalNameArgumentType {
            return object : ItemName() {
                override val showWhenEmpty: Boolean = showWhenEmpty
                private val set = allowed.toSet()
                override fun isValidItem(item: NeuInternalName): Boolean = item in set
            }
        }

        fun internalName(): InternalNameArgumentType = InternalName()

        fun internalName(isValid: (NeuInternalName) -> Boolean): InternalNameArgumentType {
            return object : InternalName() {
                private val isValid: (NeuInternalName) -> Boolean = isValid
                override fun isValidItem(item: NeuInternalName): Boolean = this.isValid(item)
            }
        }

        fun internalName(showWhenEmpty: Boolean, isValid: (NeuInternalName) -> Boolean): InternalNameArgumentType {
            return object : InternalName() {
                override val showWhenEmpty: Boolean = showWhenEmpty
                private val isValid: (NeuInternalName) -> Boolean = isValid
                override fun isValidItem(item: NeuInternalName): Boolean = this.isValid(item)
            }
        }

        fun internalName(allowed: Collection<NeuInternalName>, showWhenEmpty: Boolean = false): InternalNameArgumentType {
            return object : InternalName() {
                override val showWhenEmpty: Boolean = showWhenEmpty
                private val set = allowed.toSet()
                override fun isValidItem(item: NeuInternalName): Boolean = item in set
            }
        }
    }
}
