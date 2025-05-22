package com.github.itsempa.nautilus.mixins.transformers.skyhanni;

import at.hannibal2.skyhanni.test.command.ErrorManager;
import com.github.itsempa.nautilus.data.core.NautilusErrorManager;
import kotlin.Pair;
import kotlin.jvm.functions.Function0;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ErrorManager.class)
public class MixinErrorManager {

    @Inject(method = "logError", at = @At("HEAD"), cancellable = true, remap = false)
    private void nautilus$redirectError(Throwable originalThrowable, String message, boolean ignoreErrorCache, boolean noStackTrace, Pair<String, ?>[] extraData, boolean betaOnly, Function0<Boolean> condition, CallbackInfoReturnable<Boolean> cir) {
        if (!NautilusErrorManager.isNautilusStackTrace(originalThrowable)) return;
        cir.setReturnValue(NautilusErrorManager.logError(originalThrowable, message, ignoreErrorCache, noStackTrace, extraData, condition));
    }

}
