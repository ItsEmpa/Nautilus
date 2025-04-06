package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.deps.moulconfig.Config
import at.hannibal2.skyhanni.deps.moulconfig.annotations.Category
import com.github.itsempa.nautilus.Nautilus

class Features : Config() {
    override fun shouldAutoFocusSearchbar(): Boolean = true

    override fun getTitle(): String = "${Nautilus.MOD_NAME} ${Nautilus.VERSION} by §cEmpa"

    override fun saveNow() = ConfigManager.save()

    @Category(name = "Example", desc = "")
    var exampleCategory: ExampleCategory = ExampleCategory()
}
