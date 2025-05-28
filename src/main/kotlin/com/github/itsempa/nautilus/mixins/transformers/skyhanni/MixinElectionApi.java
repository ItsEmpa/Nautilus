package com.github.itsempa.nautilus.mixins.transformers.skyhanni;

import com.github.itsempa.nautilus.events.MayorDataUpdateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo // Use pseudo in case something changes
@Mixin(targets = "at.hannibal2.skyhanni.data.ElectionApi$checkHypixelApi$1")
public class MixinElectionApi {

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "invokeSuspend*", at = @At("RETURN"), remap = false)
    private void test(CallbackInfoReturnable<Object> cir) {
        MayorDataUpdateEvent.INSTANCE.post();
    }

}
