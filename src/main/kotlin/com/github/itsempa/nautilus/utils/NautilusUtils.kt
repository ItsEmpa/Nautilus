package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.deps.moulconfig.observer.Property
import at.hannibal2.skyhanni.utils.LocationUtils.isInside
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Rotations
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

// TODO: separate some functions into other util objects
object NautilusUtils {

    fun AxisAlignedBB.getHeight() = abs(maxY - minY)
    fun AxisAlignedBB.getWidth() = max(abs(maxX - minX), abs(maxZ - minZ))

    fun AxisAlignedBB.expandToInclude(pos: LorenzVec): AxisAlignedBB {
        val (x, y, z) = pos
        if (isInside(pos)) return this

        return AxisAlignedBB(
            minOf(this.minX, x),
            minOf(this.minY, y),
            minOf(this.minZ, z),
            maxOf(this.maxX, x),
            maxOf(this.maxY, y),
            maxOf(this.maxZ, z),
        )
    }

    fun AxisAlignedBB.getCenter(): LorenzVec = LorenzVec(
        (minX + maxX) / 2,
        (minY + maxY) / 2,
        (minZ + maxZ) / 2,
    )

    fun SimpleTimeMark.isInPastOrAlmost(maxError: Duration): Boolean {
        val passedSince = passedSince()
        return passedSince.isPositive() || passedSince.absoluteValue <= maxError
    }

    operator fun SkyBlockTime.minus(duration: Duration): SkyBlockTime = this.plus(-duration)

    fun <T : Any> T.asProperty(): Property<T> = Property.of(this)

    fun String.toSplitSet(delimiter: String = ","): Set<String> = split(delimiter).map(String::trim).toSet()

    fun Property<String>.toSet(lowercase: Boolean = true, delimiter: String = ","): Set<String> {
        var value = get()
        if (lowercase) value = value.lowercase()
        return value.toSplitSet(delimiter)
    }

    private val ZERO_ROTATIONS = Rotations(0f, 0f, 0f)

    fun Rotations.isZero(): Boolean = this == ZERO_ROTATIONS

    fun <T> MutableCollection<T>.clearAnd(predicate: (T) -> Unit) {
        val it = iterator()
        while (it.hasNext()) {
            predicate(it.next())
            it.remove()
        }
    }

    fun LorenzVec.asChatMessage(): String = "x: ${x.toInt()} y: ${y.toInt()} z: ${z.toInt()}"

    fun LorenzVec.asSimpleChatMessage(): String = "${x.toInt()} ${y.toInt()} ${z.toInt()}"

    private val lorenzVecPattern = "x: (?<x>[\\d-,.]+) y: (?<y>[\\d-,.]+) z: (?<z>[\\d-,.]+)".toPattern()

    fun String.asLorenzVec(): LorenzVec? {
        return lorenzVecPattern.matchMatcher(this) {
            val x = group("x").formatInt()
            val y = group("y").formatInt()
            val z = group("z").formatInt()
            return LorenzVec(x, y, z)
        }
    }

    fun String.hasWhitespace(): Boolean = any { it.isWhitespace() }

    fun String.splitLastWhitespace(): Pair<String, String> {
        val lastWhitespaceIndex = lastIndexOf(" ")
        return if (lastWhitespaceIndex == -1) {
            "" to this
        } else {
            substring(0, lastWhitespaceIndex) to substring(lastWhitespaceIndex + 1)
        }
    }

    fun LorenzVec.getBlockAABB() = boundingToOffset(1.0, 1.0, 1.0)
    fun <K, V> MutableMap<K, V>.clearAnd(predicate: (Map.Entry<K, V>) -> Unit) = entries.clearAnd(predicate)

    fun Double.roundToHalf() = (this * 2).roundToInt() / 2.0

    inline val Int.thousands get(): Int = this * 1_000
    inline val Int.millions get(): Int = this * 1_000_000
    inline val Int.skyblockDays get(): Duration = (this * 20).minutes
    inline val Int.skyblockMonths get(): Duration = (this * 31).skyblockDays
    inline val Int.skyblockYears get(): Duration = (this * 12).skyblockMonths
}
