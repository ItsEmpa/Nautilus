package com.github.itsempa.nautilus.commands.brigadier

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType

object BrigadierArguments {

    fun integer(min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): IntegerArgumentType =
        IntegerArgumentType.integer(min, max)

    fun long(min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE): LongArgumentType =
        LongArgumentType.longArg(min, max)

    fun double(min: Double = Double.MIN_VALUE, max: Double = Double.MAX_VALUE): DoubleArgumentType =
        DoubleArgumentType.doubleArg(min, max)

    fun float(min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE): FloatArgumentType =
        FloatArgumentType.floatArg(min, max)

    fun bool(): BoolArgumentType = BoolArgumentType.bool()

    fun string(): StringArgumentType = StringArgumentType.string()
    fun greedyString(): StringArgumentType = StringArgumentType.greedyString()
    fun word(): StringArgumentType = StringArgumentType.word()

}
