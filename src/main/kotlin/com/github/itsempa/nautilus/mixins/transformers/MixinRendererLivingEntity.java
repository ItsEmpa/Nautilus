package com.github.itsempa.nautilus.mixins.transformers;

import com.github.itsempa.nautilus.features.misc.Spinny;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RendererLivingEntity.class)
public class MixinRendererLivingEntity<T extends EntityLivingBase> {

    @Inject(
        method = "rotateCorpse",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumChatFormatting;getTextWithoutFormattingCodes(Ljava/lang/String;)Ljava/lang/String;")
    )
    void createSpin(T bat, float f, float g, float partialTicks, CallbackInfo ci) {
        Spinny.rotatePlayer(bat, partialTicks);
    }

}
