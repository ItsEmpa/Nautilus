package com.github.itsempa.nautilus.mixins.transformers.skyhanni;

import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.config.core.config.gui.GuiPositionEditor;
import com.github.itsempa.nautilus.mixins.hooks.NautilusPositionData;
import com.github.itsempa.nautilus.mixins.hooks.PositionHook;
import kotlin.Pair;
import kotlin.collections.IndexedValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(GuiPositionEditor.class)
public class MixinGuiPositionEditor {

    @Unique
    private boolean nautilus$isCurrentPosHovering = false;

    @Unique
    private boolean nautilus$isCurrentPosNautilus = false;

    @Unique // #60A0D0
    private static final int nautilus$SELECTED_COLOR = 0x8060a0d0;

    @Unique // #204060
    private static final int nautilus$UNSELECTED_COLOR = 0x80204060;

    @Inject(method = "getTextForPos", at = @At("TAIL"), cancellable = true, remap = false)
    private void getTextForPos(Position pos, CallbackInfoReturnable<List<String>> cir) {
        PositionHook.redirectHoverText(pos, cir);
    }

    @Inject(method = "renderLabels", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.AFTER), remap = false)
    private void renderLabels(int hoveredPos, CallbackInfo ci) {
        // TODO: add "Nautilus" text to it?
    }

    @Inject(method = "renderRectangles", at = @At(value = "INVOKE", target = "Lat/hannibal2/skyhanni/utils/GuiRenderUtils;drawRect(IIIII)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    private void getPositionLocals(CallbackInfoReturnable<Integer> cir, int hoveredPos, Pair<Integer, Integer> var2, int mouseX, int mouseY, boolean alreadyHadHover, Iterator<IndexedValue<Position>> var6, IndexedValue<Position> var7, int index, Position position, int elementWidth, int elementHeight, int x, int y, boolean isHovering, int gray, int selected) {
        nautilus$isCurrentPosHovering = isHovering;
        nautilus$isCurrentPosNautilus = NautilusPositionData.isNautilus(position);
    }

    @ModifyArg(method = "renderRectangles", at = @At(value = "INVOKE", target = "Lat/hannibal2/skyhanni/utils/GuiRenderUtils;drawRect(IIIII)V"), index = 4, remap = false)
    private int redirectRectangleColor(int original) {
        if (!nautilus$isCurrentPosNautilus) return original;
        return nautilus$isCurrentPosHovering ? nautilus$SELECTED_COLOR : nautilus$UNSELECTED_COLOR;
    }
}
