package com.github.itsempa.nautilus.utils.safe

import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.renderables.Renderable
import java.awt.Color

object SafeUtils {

    // TODO: Deprecate with the actual RenderableString when the name gets changed in SkyHanni
    @Suppress("DEPRECATION")
    fun stringRenderable(
        text: String,
        scale: Double = 1.0,
        color: Color = Color.WHITE,
        horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
        verticalAlign: VerticalAlignment = VerticalAlignment.CENTER,
    ): Renderable {
        return Renderable.string(text, scale, color, horizontalAlign, verticalAlign)
    }

}
