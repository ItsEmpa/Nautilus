package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.deps.moulconfig.Config
import at.hannibal2.skyhanni.deps.moulconfig.Social
import at.hannibal2.skyhanni.deps.moulconfig.annotations.Category
import at.hannibal2.skyhanni.deps.moulconfig.common.MyResourceLocation
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.config.storage.Storage
import com.google.gson.annotations.Expose

class Features : Config() {
    private val discord = MyResourceLocation("skyhanni", "social/discord.png")
    private val github = MyResourceLocation("skyhanni", "social/github.png")

    override fun shouldAutoFocusSearchbar(): Boolean = false

    override fun getTitle(): String = "${Nautilus.MOD_NAME} ${Nautilus.VERSION} by §cEmpa"

    override fun saveNow() = ConfigManager.save()

    override fun getSocials(): List<Social> {
        return listOf(
            Social.forLink("Discord", discord, "https://discord.gg/KM3dKjbWqg"),
            Social.forLink("GitHub", github, "https://github.com/ItsEmpa/Nautilus"),
        )
    }

    @Expose
    @Category(name = "About", desc = "")
    val about: About = About()

    @Expose
    val storage: Storage = Storage()
}
