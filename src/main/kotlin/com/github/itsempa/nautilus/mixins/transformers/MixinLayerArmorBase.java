package com.github.itsempa.nautilus.mixins.transformers;

import com.github.itsempa.nautilus.mixins.hooks.ArmorGlintHook;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerArmorBase.class)
public class MixinLayerArmorBase<T extends ModelBase> {

    @Inject(method = "renderGlint", at = @At("HEAD"), cancellable = true)
    private void shouldRenderGlint(EntityLivingBase entitylivingbaseIn, T modelbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        if (ArmorGlintHook.shouldHide()) ci.cancel();
    }

}
