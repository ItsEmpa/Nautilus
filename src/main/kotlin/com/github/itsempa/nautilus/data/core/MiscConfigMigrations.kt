package com.github.itsempa.nautilus.data.core

import at.hannibal2.skyhanni.api.event.HandleEvent
import com.github.itsempa.nautilus.config.core.loader.NautilusConfigFixEvent
import me.owdding.ktmodules.Module

/** Class for config migrations that don't make sense to have in any other specific class. */
@Module
object MiscConfigMigrations {

    @HandleEvent
    fun onConfigFix(event: NautilusConfigFixEvent) = with(event) {

    }

}
