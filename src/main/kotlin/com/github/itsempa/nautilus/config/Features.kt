package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.deps.moulconfig.Config
import at.hannibal2.skyhanni.deps.moulconfig.annotations.Category
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.Nautilus.managedConfig

class Features : Config() {
    override fun shouldAutoFocusSearchbar(): Boolean = true

    override fun getTitle(): String = "${Nautilus.MOD_NAME} ${Nautilus.VERSION}"

    override fun saveNow() = managedConfig.saveToFile()

    @Category(name = "Example", desc = "")
    var exampleCategory: ExampleCategory = ExampleCategory()
}
