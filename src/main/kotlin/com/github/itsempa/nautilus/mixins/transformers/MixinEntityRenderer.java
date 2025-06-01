package com.github.itsempa.nautilus.mixins.transformers;

import com.github.itsempa.nautilus.Nautilus;
import com.github.itsempa.nautilus.config.misc.ThirdPersonConfig;
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
        ThirdPersonConfig config = Nautilus.getFeature().getMisc().getThirdPerson();
        if (this.mc.gameSettings.thirdPersonView == 0 || !config.getThirdPersonFov()) return original;
        return config.getFov();
    }

}
