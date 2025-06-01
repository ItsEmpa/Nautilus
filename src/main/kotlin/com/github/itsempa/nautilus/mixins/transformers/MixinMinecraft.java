package com.github.itsempa.nautilus.mixins.transformers;

import com.github.itsempa.nautilus.Nautilus;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @ModifyConstant(
        method = "runTick", constant = @Constant(intValue = 2),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;keyBindTogglePerspective:Lnet/minecraft/client/settings/KeyBinding;"),
            to = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;keyBindSmoothCamera:Lnet/minecraft/client/settings/KeyBinding;")
        )
    )
    private int nautilus$noFrontCamera(int original) {
        boolean shouldRemoveFrontCamera = Nautilus.getFeature().getMisc().getThirdPerson().getRemoveFrontCamera();
        return shouldRemoveFrontCamera ? 1 : original;
    }

}
