package com.github.itsempa.nautilus.mixins.transformers.skyhanni;

import at.hannibal2.skyhanni.config.core.config.Position;
import com.github.itsempa.nautilus.mixins.hooks.NautilusPositionData;
import com.github.itsempa.nautilus.mixins.hooks.PositionHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Position.class)
public class MixinPosition implements NautilusPositionData {

    @Override
    public boolean isNautilus() {
        return nautilus$isNautilus;
    }

    @Override
    public void setNautilus(boolean nautilus) {
        nautilus$isNautilus = nautilus;
    }

    @Unique
    private boolean nautilus$isNautilus = false;

    @Inject(method = "canJumpToConfigOptions", at = @At(value = "INVOKE", target = "Lat/hannibal2/skyhanni/config/ConfigGuiManager;getEditorInstance()Lat/hannibal2/skyhanni/deps/moulconfig/gui/MoulConfigEditor;"), cancellable = true, remap = false)
    private void nautilus$canJumpToConfigOptions(CallbackInfoReturnable<Boolean> cir) {
        if (this.nautilus$isNautilus) {
            cir.setReturnValue(PositionHook.canJumpToNautilusConfig((Position) (Object) this));
        }
    }

    @Inject(method = "jumpToConfigOptions", at = @At("HEAD"), cancellable = true, remap = false)
    private void nautilus$jumpToConfigOptions(CallbackInfo ci) {
        if (this.nautilus$isNautilus) {
            PositionHook.jumpToNautilusConfig((Position) (Object) this);
            ci.cancel();
        }
    }

}
