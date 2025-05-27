package com.github.itsempa.nautilus.mixins.transformers;

import com.github.itsempa.nautilus.Nautilus;
import com.github.itsempa.nautilus.config.misc.ThirdPersonFovConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow
    private Minecraft mc;

    @ModifyVariable(method = "getFOVModifier", at = @At(value = "STORE", ordinal = 1), ordinal = 1)
    public float modifyThirdPersonFov(float original) {
        ThirdPersonFovConfig config = Nautilus.getFeature().getMisc().getFov();
        if (this.mc.gameSettings.thirdPersonView == 0 || !config.getEnabled()) return original;
        return config.getFov();
    }

}
