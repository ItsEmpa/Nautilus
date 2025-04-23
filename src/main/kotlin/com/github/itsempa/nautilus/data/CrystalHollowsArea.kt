package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.utils.LocationUtils.isPlayerInside
import at.hannibal2.skyhanni.utils.LorenzVec
import net.minecraft.util.AxisAlignedBB

// Heat is active at Y=64.0 and below as of SkyBlock 0.20.1. We draw the line
// one above to accurately show whether the player is inside the Magma Fields.
private const val HEAT_HEIGHT = 65.0
private const val MAX_HEIGHT = 190.0

private const val MIN_X = 0.0
private const val MIDDLE_X = 513.0
private const val MAX_X = 1024.0

private const val MIN_Z = 0.0
private const val MIDDLE_Z = 513.0
private const val MAX_Z = 1024.0

private val JUNGLE_MIN = LorenzVec(MIN_X, HEAT_HEIGHT, MIN_Z)
private val JUNGLE_MAX = LorenzVec(MIDDLE_X, MAX_HEIGHT, MIDDLE_Z)

private val MITHRIL_MIN = LorenzVec(MIDDLE_X, HEAT_HEIGHT, MIN_Z)
private val MITHRIL_MAX = LorenzVec(MAX_X, MAX_HEIGHT, MIDDLE_Z)

private val GOBLIN_MIN = LorenzVec(MIN_X, HEAT_HEIGHT, MIDDLE_Z)
private val GOBLIN_MAX = LorenzVec(MIDDLE_X, MAX_HEIGHT, MAX_Z)

private val PRECURSOR_MIN = LorenzVec(MIDDLE_X, HEAT_HEIGHT, MIDDLE_Z)
private val PRECURSOR_MAX = LorenzVec(MAX_X, MAX_HEIGHT, MAX_Z)

private val MAGMA_MIN = LorenzVec(MIN_X, 0.0, MIN_Z)
private val MAGMA_MAX = LorenzVec(MAX_X, HEAT_HEIGHT, MAX_Z)

enum class CrystalHollowsArea(private val aabb: AxisAlignedBB) {
    JUNGLE(JUNGLE_MIN, JUNGLE_MAX),
    MITHRIL_DEPOSITS(MITHRIL_MIN, MITHRIL_MAX),
    GOBLIN_HOLDOUT(GOBLIN_MIN, GOBLIN_MAX),
    PRECURSOR_REMNANTS(PRECURSOR_MIN, PRECURSOR_MAX),
    MAGMA_FIELDS(MAGMA_MIN, MAGMA_MAX)
    ;

    constructor(from: LorenzVec, to: LorenzVec) : this(from.axisAlignedTo(to))

    fun inArea(): Boolean = aabb.isPlayerInside()
}
