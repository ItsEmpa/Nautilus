package com.github.itsempa.nautilus.mixins.hooks

import com.github.itsempa.nautilus.Nautilus
import net.minecraft.block.BlockLiquid
import net.minecraft.block.material.Material
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos.MutableBlockPos
import net.minecraft.util.MathHelper
import net.minecraft.world.World

object LavaBobberHook {

    private val config get() = Nautilus.feature.render.fixLavaBobbers

    @JvmStatic
    fun isEnabled() = config

    @JvmStatic
    fun fixBobberLava(instance: World, aabb: AxisAlignedBB): Boolean {
        return instance.isAABBInMaterials(aabb, Material.water, Material.lava)
    }

    private fun World.isAABBInMaterials(aabb: AxisAlignedBB, vararg materials: Material): Boolean {
        val minX = MathHelper.floor_double(aabb.minX)
        val maxX = MathHelper.floor_double(aabb.maxX + 1.0)
        val minY = MathHelper.floor_double(aabb.minY)
        val maxY = MathHelper.floor_double(aabb.maxY + 1.0)
        val minZ = MathHelper.floor_double(aabb.minZ)
        val maxZ = MathHelper.floor_double(aabb.maxZ + 1.0)

        val pos = MutableBlockPos()

        for (x in minX until maxX) {
            for (y in minY until maxY) {
                for (z in minZ until maxZ) {
                    pos.set(x, y, z)
                    val state = getBlockState(pos)
                    val block = state.block

                    for (material in materials) {
                        block.isAABBInsideMaterial(this, pos, aabb, material)?.let { return it }

                        if (block.material == material) {
                            val level = state.getValue(BlockLiquid.LEVEL)
                            val surfaceY = y + 1.0 - if (level < 8) level / 8.0 else 0.0

                            if (surfaceY >= aabb.minY) return true
                        }
                    }
                }
            }
        }

        return false
    }


}
