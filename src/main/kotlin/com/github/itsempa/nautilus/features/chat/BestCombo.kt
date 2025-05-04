package com.github.itsempa.nautilus.features.chat

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.NautilusStorage
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.events.combo.ComboEndEvent
import com.github.itsempa.nautilus.events.combo.ComboUpdateEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat

@Module
object BestCombo {

    private val config get() = Nautilus.feature.chat.bestCombo

    private val storage get() = NautilusStorage.profile

    var bestCombo: Int
        get() = storage.bestCombo
        private set(value) {
            storage.bestCombo = value
        }

    var isInBestCombo: Boolean = false
        private set

    @HandleEvent
    fun onComboUpdate(event: ComboUpdateEvent) {
        if (event.combo > bestCombo && !isInBestCombo) {
            isInBestCombo = true
            val currentBestCombo = bestCombo
            if (currentBestCombo != 0) sendMessage("You beat your previous best combo of §l§6${currentBestCombo.addSeparators()}§3!")
        }
    }

    @HandleEvent
    fun onComboFinish(event: ComboEndEvent) {
        if (!isInBestCombo) return
        isInBestCombo = true
        val currentBestCombo = bestCombo
        val combo = event.combo
        bestCombo = combo
        if (currentBestCombo == 0) {
            sendMessage("You got a new best combo of §l§6${combo.addSeparators()}§3!")
        } else {
            sendMessage(
                "You beat your previous best combo of ${currentBestCombo.addSeparators()} " +
                    "with a combo of §l§6${combo.addSeparators()}§3!",
            )
        }

    }

    private fun sendMessage(message: String) {
        if (config) NautilusChat.chat(message)
    }

    @HandleEvent
    fun onCommand(event: NautilusCommandRegistrationEvent) {
        event.register("ntbestcombo") {
            this.description = "Shows what the longest combo you have gotten is."
            this.category = CommandCategory.USERS_ACTIVE
            callback {
                val combo = bestCombo
                if (combo == 0) {
                    NautilusChat.chat("You haven't gotten a combo yet :(")
                } else {
                    NautilusChat.chat("Your best combo is: §l§6${combo.addSeparators()}§3!")
                }
            }
        }
        event.register("ntresetbestcombo") {
            this.description = "Resets your best combo."
            this.category = CommandCategory.USERS_RESET
            callback {
                bestCombo = 0
                NautilusChat.chat("Your best combo has been reset.")
            }
        }
    }

}
