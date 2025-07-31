package com.github.itsempa.nautilus.utils.safe

import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.primitives.StringRenderable

object SafeUtils {

    // TODO: Deprecate with the actual RenderableString when the name gets changed in SkyHanni
    @Suppress("DEPRECATION")
    fun stringRenderable(text: String): Renderable = StringRenderable.from(text)

}
