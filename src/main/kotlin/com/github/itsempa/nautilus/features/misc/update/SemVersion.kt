package com.github.itsempa.nautilus.features.misc.update

import at.hannibal2.skyhanni.utils.json.SimpleStringTypeAdapter
import com.github.itsempa.nautilus.utils.NautilusNullableUtils.orZero
import com.google.gson.JsonElement
import moe.nea.libautoupdate.CurrentVersion

data class SemVersion(val major: Int, val minor: Int, val patch: Int) : Comparable<SemVersion>, CurrentVersion {

    inline val asString: String get() = toString()

    override fun toString(): String = "$major.$minor.$patch"

    override fun display(): String = asString

    override fun compareTo(other: SemVersion): Int {
        return when {
            major != other.major -> major.compareTo(other.major)
            minor != other.minor -> minor.compareTo(other.minor)
            else -> patch.compareTo(other.patch)
        }
    }

    override fun isOlderThan(element: JsonElement?): Boolean {
        val version = element?.asString ?: return true
        val semVer = fromString(version)
        return this < semVer
    }

    operator fun rangeTo(other: SemVersion): VersionRange = VersionRange(this, other)

    companion object {

        fun fromString(version: String): SemVersion {
            val parts = version.split('.')
            return SemVersion(
                parts.getOrNull(0)?.toIntOrNull().orZero(),
                parts.getOrNull(1)?.toIntOrNull().orZero(),
                parts.getOrNull(2)?.toIntOrNull().orZero(),
            )
        }

        val TYPE_ADAPTER = SimpleStringTypeAdapter(SemVersion::asString, ::fromString)
    }
}

// Creates a Range of versions that don't include the start one but include the end one
data class VersionRange(val startExclusive: SemVersion, val endInclusive: SemVersion) {
    operator fun contains(version: SemVersion): Boolean = version > startExclusive && version <= endInclusive
    fun isEmpty(): Boolean = startExclusive > endInclusive
}
