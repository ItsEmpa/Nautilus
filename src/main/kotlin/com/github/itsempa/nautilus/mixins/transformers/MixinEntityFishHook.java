package com.github.itsempa.nautilus.mixins.transformers;

import com.github.itsempa.nautilus.mixins.hooks.LavaBobberHook;
import net.minecraft.block.material.Material;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityFishHook.class)
public class MixinEntityFishHook {

    // TODO: figure out a way to only modify the rendering of the bobber
    @Redirect(
        method = "onUpdate",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isAABBInMaterial(Lnet/minecraft/util/AxisAlignedBB;Lnet/minecraft/block/material/Material;)Z"
        )
    )
    public boolean fixBobberLava(World instance, AxisAlignedBB aabb, Material material) {
        if (!LavaBobberHook.isEnabled()) return instance.isAABBInMaterial(aabb, material);
        return LavaBobberHook.fixBobberLava(instance, aabb);
    }

}
