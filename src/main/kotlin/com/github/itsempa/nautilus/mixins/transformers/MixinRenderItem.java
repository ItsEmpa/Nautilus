package com.github.itsempa.nautilus.mixins.transformers;

import com.github.itsempa.nautilus.mixins.hooks.ArmorGlintHook;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderItem.class)
public class MixinRenderItem {

    @Redirect(
        method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasEffect()Z")
    )
    private boolean shouldRenderGlint(ItemStack stack) {
        return stack.hasEffect() && !ArmorGlintHook.shouldHideGlint(stack);
    }
}
