package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.utils.NeuInternalName
import com.github.itsempa.nautilus.data.repo.FishingDropsRepo
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.events.NautilusRepositoryReloadEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat

@Module(devOnly = true)
object RepoTest {

    private var items = mapOf<NeuInternalName, List<String>>()

    @HandleEvent
    fun onRepoReload(event: NautilusRepositoryReloadEvent) {
        items = event.getConstant<FishingDropsRepo>("FishingDrops").items
    }

    private fun sendTest() {
        val msg = items.entries.joinToString { (internalName, mobs) ->
            var text = "${internalName.asString()}:\n"
            mobs.forEach { mob ->
                text += "  - $mob\n"
            }
            text
        }
        NautilusChat.chat(msg)
    }

    @HandleEvent
    fun onCommandRegistration(event: NautilusCommandRegistrationEvent) {
        event.register("ntrepotest") {
            this.callback {
                sendTest()
            }
        }
    }

}
