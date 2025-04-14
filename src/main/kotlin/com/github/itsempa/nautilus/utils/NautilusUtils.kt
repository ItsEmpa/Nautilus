package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.deps.moulconfig.observer.Property
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.LocationUtils.union
import at.hannibal2.skyhanni.utils.RenderUtils.exactBoundingBox
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.getLorenzVec
import com.github.itsempa.nautilus.Nautilus
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import java.time.Instant
import kotlin.math.abs
import kotlin.time.Duration

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Comparable<T>> min(a: T, b: T): T = if (a < b) a else b

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Comparable<T>> max(a: T, b: T): T = if (a > b) a else b

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

    operator fun SkyBlockTime.minus(duration: Duration): SkyBlockTime {
        val millis = toMillis() - duration.inWholeMilliseconds
        return SkyBlockTime.fromInstant(Instant.ofEpochMilli(millis))
    }

    fun <K, V> MutableMap<K, V>.removeIf(predicate: (Map.Entry<K, V>) -> Boolean) = entries.removeIf(predicate)

    inline val Mob.hasDied: Boolean get() = baseEntity.hasDied

    fun Mob.getLorenzVec() = baseEntity.getLorenzVec()

    fun SkyHanniRenderWorldEvent.exactBoundingBoxExtraEntities(mob: Mob): AxisAlignedBB {
        val aabb = exactBoundingBox(mob.baseEntity)
        return aabb.union(
            mob.extraEntities.map { exactBoundingBox(it) },
        ) ?: aabb
    }

    inline val Mob.entityId get() = baseEntity.entityId

    fun SkyHanniRenderWorldEvent.exactLocation(mob: Mob) = exactLocation(mob.baseEntity)

    inline val EntityLivingBase.hasDied get() = isDead || health <= 0f

    fun <T : Enum<T>> Enum<T>.toFormattedName(): String =
        name.split("_").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }

    fun <T : Any> T.asProperty(): Property<T> = Property.of(this)

    inline fun <reified T> Any.cast(): T = this as T
    inline fun <reified T> Any.castOrNull(): T? = this as? T

    inline fun <T, C : Comparable<C>> minBy(a: T, b: T, comparator: (T) -> C): T = if (comparator(a) < comparator(b)) a else b
    inline fun <T, C : Comparable<C>> maxBy(a: T, b: T, comparator: (T) -> C): T = if (comparator(a) > comparator(b)) a else b

    inline val Int.thousands get(): Int = this * 1_000
    inline val Int.millions get(): Int = this * 1_000_000

    @Suppress("NOTHING_TO_INLINE")
    inline fun Int?.orZero(): Int = this ?: 0

    @Suppress("NOTHING_TO_INLINE")
    inline fun Double?.orZero(): Double = this ?: 0.0

    @Suppress("NOTHING_TO_INLINE")
    inline fun Float?.orZero(): Float = this ?: 0f

    @Suppress("NOTHING_TO_INLINE")
    inline fun Long?.orZero(): Long = this ?: 0L

    @Suppress("NOTHING_TO_INLINE")
    inline fun Boolean?.orFalse(): Boolean = this ?: false
}
