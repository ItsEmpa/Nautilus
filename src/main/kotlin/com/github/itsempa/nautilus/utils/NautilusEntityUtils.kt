package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.LocationUtils.union
import at.hannibal2.skyhanni.utils.RenderUtils.exactBoundingBox
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.ticks
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB

object NautilusEntityUtils {
    inline val Mob.hasDied: Boolean get() = baseEntity.hasDied

    fun Mob.getLorenzVec() = baseEntity.getLorenzVec()

    fun SkyHanniRenderWorldEvent.exactBoundingBoxExtraEntities(mob: Mob): AxisAlignedBB {
        val aabb = exactBoundingBox(mob.baseEntity)
        return aabb.union(
            mob.extraEntities.map { exactBoundingBox(it) },
        ) ?: aabb
    }

    fun Mob.getBoundingBoxExtraEntities(): AxisAlignedBB {
        val aabb = baseEntity.entityBoundingBox
        return aabb.union(
            extraEntities.map { it.entityBoundingBox },
        ) ?: aabb
    }

    inline val Mob.entityId get() = baseEntity.entityId

    fun SkyHanniRenderWorldEvent.exactLocation(mob: Mob) = exactLocation(mob.baseEntity)

    inline val EntityLivingBase.hasDied get() = isDead || health <= 0f

    inline val Entity.spawnTime: SimpleTimeMark get() = SimpleTimeMark.now() - ticksExisted.ticks
}
