package com.github.itsempa.nautilus.mixins.transformers.modidhider;

import com.github.itsempa.nautilus.Nautilus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.misc.Funny", remap = false)
public class MixinSkytilsFunnny {
    @Inject(method = "joinedSkyblock*", at = @At("HEAD"), expect = 0, cancellable = true, remap = false)
    public void onJoinedSkyblockHead(CallbackInfo ci) {
        if (!Nautilus.HIDE_MOD_ID) return;
        ci.cancel();
    }
}
