package com.github.itsempa.nautilus.mixins.transformers.skyhanni;

import at.hannibal2.skyhanni.config.ConfigManager;
import com.github.itsempa.nautilus.Nautilus;
import kotlin.text.StringsKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ConfigManager.class)
public class MixinConfigManager {

    // This is to allow for config links from other mods to also work.
    @Redirect(method = "findPositionLinks", at = @At(value = "INVOKE", target = "Lkotlin/text/StringsKt;startsWith$default(Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Z"), remap = false)
    private boolean nautilus$findPositionLinks(String startsWith, String prefix, boolean ignoreCase, int startIndex, Object default_) {
        return StringsKt.startsWith(startsWith, prefix, false) || StringsKt.startsWith(startsWith, Nautilus.PATH, true);
    }

}
