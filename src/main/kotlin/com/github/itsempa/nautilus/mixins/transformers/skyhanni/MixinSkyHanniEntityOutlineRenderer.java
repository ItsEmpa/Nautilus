package com.github.itsempa.nautilus.mixins.transformers.skyhanni;

import at.hannibal2.skyhanni.utils.EntityOutlineRenderer;
import com.github.itsempa.nautilus.data.NautilusOutlineFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityOutlineRenderer.class)
public class MixinSkyHanniEntityOutlineRenderer {

    @Inject(method = "isEnabled", at = @At("TAIL"), remap = false, cancellable = true)
    private void isEnabledRedirect(CallbackInfoReturnable<Boolean> cir) {
        if (NautilusOutlineFeatures.getAnyEnabled()) cir.setReturnValue(true);
    }
}
