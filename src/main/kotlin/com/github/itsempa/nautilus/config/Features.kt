package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.deps.moulconfig.Config
import at.hannibal2.skyhanni.deps.moulconfig.Social
import at.hannibal2.skyhanni.deps.moulconfig.annotations.Category
import at.hannibal2.skyhanni.deps.moulconfig.common.MyResourceLocation
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.config.chat.ChatConfig
import com.github.itsempa.nautilus.config.dev.DevConfig
import com.github.itsempa.nautilus.config.gui.GuiConfig
import com.github.itsempa.nautilus.config.misc.MiscConfig
import com.github.itsempa.nautilus.config.render.RenderConfig
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
            Social.forLink("Discord", discord, Nautilus.DISCORD_INVITE),
            Social.forLink("GitHub", github, Nautilus.GITHUB),
        )
    }

    @Expose
    @Category(name = "About", desc = "")
    val about: About = About()

    @Expose
    @Category(name = "Render", desc = "")
    val render: RenderConfig = RenderConfig()

    @Expose
    @Category(name = "Gui", desc = "")
    val gui: GuiConfig = GuiConfig()

    @Expose
    @Category(name = "Chat", desc = "")
    val chat: ChatConfig = ChatConfig()

    @Expose
    @Category(name = "Misc", desc = "")
    val misc: MiscConfig = MiscConfig()

    @Expose
    @Category(name = "Dev", desc = "")
    val dev: DevConfig = DevConfig()

    @Expose
    val storage: Storage = Storage()
}
