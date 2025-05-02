package com.github.itsempa.nautilus.config.dev

import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorButton
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorText
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import com.github.itsempa.nautilus.data.NautilusRepoManager
import com.google.gson.annotations.Expose

class RepoConfig {

    @Transient
    @ConfigOption(name = "Reset Location", desc = "Reset your repository location to the default.")
    @ConfigEditorButton(buttonText = "Reset")
    val reset = Runnable {
        this.user = NautilusRepoManager.DEFAULT_USER
        this.name = NautilusRepoManager.DEFAULT_NAME
        this.branch = NautilusRepoManager.DEFAULT_BRANCH
    }

    @Transient
    @ConfigOption(name = "Update Repo", desc = "Download the repository files again.")
    @ConfigEditorButton(buttonText = "Update")
    val update = Runnable(NautilusRepoManager::updateRepo)

    @Expose
    @ConfigOption(name = "Repository User", desc = "The Repository Branch, default ${NautilusRepoManager.DEFAULT_USER}")
    @ConfigEditorText
    var user: String = NautilusRepoManager.DEFAULT_USER

    @Expose
    @ConfigOption(name = "Repository Name", desc = "The Repository Name, default: ${NautilusRepoManager.DEFAULT_NAME}")
    @ConfigEditorText
    var name: String = NautilusRepoManager.DEFAULT_NAME

    @Expose
    @ConfigOption(name = "Repository Branch", desc = "The Repository Branch, default: ${NautilusRepoManager.DEFAULT_BRANCH}")
    @ConfigEditorText
    var branch: String = NautilusRepoManager.DEFAULT_BRANCH

    operator fun component1() = user
    operator fun component2() = name
    operator fun component3() = branch
}
