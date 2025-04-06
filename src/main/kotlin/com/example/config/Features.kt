package com.example.config

import at.hannibal2.skyhanni.deps.moulconfig.Config
import at.hannibal2.skyhanni.deps.moulconfig.annotations.Category
import com.example.ExampleMod
import com.example.ExampleMod.managedConfig

class Features : Config() {
    override fun shouldAutoFocusSearchbar(): Boolean = true

    override fun getTitle(): String = "${ExampleMod.MOD_NAME} ${ExampleMod.VERSION}"

    override fun saveNow() = managedConfig.saveToFile()

    @Category(name = "Example", desc = "")
    var exampleCategory: ExampleCategory = ExampleCategory()
}
