package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.deps.moulconfig.observer.Property
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.SkyBlockTime.Companion.plus
import com.github.itsempa.nautilus.Nautilus
import net.minecraft.util.AxisAlignedBB
import kotlin.math.abs
import kotlin.time.Duration

// TODO: separate some functions into other util objects
object NautilusUtils {
    // TODO: replace with own custom error manager
    fun logErrorWithData(
        throwable: Throwable,
        message: String,
        vararg extraData: Pair<String, Any?>,
        ignoreErrorCache: Boolean = false,
        noStackTrace: Boolean = false,
    ) {
        ErrorManager.logErrorWithData(
            throwable,
            "§c${Nautilus.MOD_NAME.uppercase()} ERROR!! $message. Please report this to Empa.",
            extraData = extraData,
            ignoreErrorCache = ignoreErrorCache,
            noStackTrace = noStackTrace,
            betaOnly = false,
        )
    }

    fun AxisAlignedBB.getHeight() = abs(maxY - minY)

    fun AxisAlignedBB.getCenter(): LorenzVec = LorenzVec(
        (minX + maxX) / 2,
        (minY + maxY) / 2,
        (minZ + maxZ) / 2,
    )
    
    fun SimpleTimeMark.isInPastOrAlmost(maxError: Duration): Boolean {
        val passedSince = passedSince()
        return passedSince.isPositive() || passedSince.absoluteValue <= maxError
    }

    operator fun SkyBlockTime.compareTo(other: SkyBlockTime): Int {
        return when {
            year != other.year -> year.compareTo(other.year)
            month != other.month -> month.compareTo(other.month)
            day != other.day -> day.compareTo(other.day)
            hour != other.hour -> hour.compareTo(other.hour)
            minute != other.minute -> minute.compareTo(other.minute)
            else -> second.compareTo(other.second)
        }
    }

    operator fun SkyBlockTime.minus(duration: Duration): SkyBlockTime = this.plus(-duration)

    fun <K, V> MutableMap<K, V>.removeIf(predicate: (Map.Entry<K, V>) -> Boolean) = entries.removeIf(predicate)

    fun <T : Any> T.asProperty(): Property<T> = Property.of(this)

    fun String.toSplitSet(delimiter: String = ","): Set<String> = split(delimiter).map(String::trim).toSet()

    fun Property<String>.toSet(lowercase: Boolean = true, delimiter: String = ","): Set<String> {
        var value = get()
        if (lowercase) value = value.lowercase()
        return value.toSplitSet(delimiter)
    }

    inline val Int.thousands get(): Int = this * 1_000
    inline val Int.millions get(): Int = this * 1_000_000
}
