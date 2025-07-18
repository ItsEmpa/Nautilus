package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import kotlin.math.absoluteValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object NautilusTimeUtils {
    private val EMPTY_SET = emptyEnumSet<NautilusTimeUnit>()

    fun Duration.customFormat(
        biggestUnit: NautilusTimeUnit = NautilusTimeUnit.YEAR,
        smallestUnit: NautilusTimeUnit = NautilusTimeUnit.SECOND,
        showDeciseconds: Boolean = this.absoluteValue < 1.seconds,
        longName: Boolean = false,
        maxUnits: Int = -1,
        showSmallerUnits: Boolean = false,
        showNegativeAsSoon: Boolean = true,
        ignoredUnits: Set<NautilusTimeUnit> = EMPTY_SET
    ): String {
        var millis = inWholeMilliseconds.absoluteValue
        val prefix = if (isNegative()) {
            if (showNegativeAsSoon) return "Soon"
            "-"
        } else ""

        val parts = enumMapOf<NautilusTimeUnit, Int>()

        for (unit in NautilusTimeUnit.entries) {
            if (unit in ignoredUnits) continue
            val factor = unit.factor
            if (factor > biggestUnit.factor) continue
            if (factor < smallestUnit.factor) continue

            val value = (millis / factor).toInt()

            if (value > 0 || (showSmallerUnits && parts.isNotEmpty())) {
                parts[unit] = value
                millis %= factor
            }

            if (maxUnits != -1 && parts.size >= maxUnits) break
        }

        if (absoluteValue < 1.seconds) {
            if (smallestUnit > NautilusTimeUnit.SECOND) return prefix + "0"
            if (!showDeciseconds) return "${prefix}0${NautilusTimeUnit.SECOND.getName(0, longName)}"
            val formattedDs = (millis / 100).toInt()
            return "${prefix}0.$formattedDs${NautilusTimeUnit.SECOND.getName(formattedDs, longName)}"
        }

        val result = buildString {
            for ((unit, value) in parts) {
                if (isNotEmpty()) append(" ")
                val formatted = value.addSeparators()
                val text = if (unit == NautilusTimeUnit.SECOND && showDeciseconds) {
                    val formattedDs = (millis / 100).toInt()
                    "$formatted.$formattedDs"
                } else formatted
                append(text + unit.getName(value, longName))
            }
        }

        return prefix + result
    }
}

private const val FACTOR_SECONDS = 1000L
private const val FACTOR_MINUTES = FACTOR_SECONDS * 60
private const val FACTOR_HOURS = FACTOR_MINUTES * 60
private const val FACTOR_DAYS = FACTOR_HOURS * 24
private const val FACTOR_WEEKS = FACTOR_DAYS * 7
private const val FACTOR_MONTH = FACTOR_DAYS * 30
private const val FACTOR_YEARS = (FACTOR_DAYS * 365.25).toLong()

enum class NautilusTimeUnit(val factor: Long, private val shortName: String, private val longName: String) {
    YEAR(FACTOR_YEARS, "y", "Year"),
    MONTH(FACTOR_MONTH, "mo", "Month"),
    WEEK(FACTOR_WEEKS, "w", "Week"),
    DAY(FACTOR_DAYS, "d", "Day"),
    HOUR(FACTOR_HOURS, "h", "Hour"),
    MINUTE(FACTOR_MINUTES, "m", "Minute"),
    SECOND(FACTOR_SECONDS, "s", "Second"),
    ;

    fun getName(value: Int, longFormat: Boolean) = if (longFormat) {
        " $longName" + if (value == 1) "" else "s"
    } else shortName
}
