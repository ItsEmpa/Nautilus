package com.github.itsempa.nautilus.features.chat

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPriceOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.repoItemName
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat

@Module
object FishingPetProfit {

    private const val EPIC_MAX_XP = 18_608_500
    private const val LEG_MAX_XP = 25_353_230

    // TODO: move to repo?
    private val pets = buildList {
        addPet("AMMONITE", LorenzRarity.LEGENDARY)
        addPet("BABY_YETI", LorenzRarity.LEGENDARY, LorenzRarity.EPIC)
        addPet("BLUE_WHALE", LorenzRarity.LEGENDARY, LorenzRarity.EPIC)
        addPet("DOLPHIN", LorenzRarity.LEGENDARY, LorenzRarity.EPIC)
        addPet("FLYING_FISH", LorenzRarity.MYTHIC, LorenzRarity.LEGENDARY, LorenzRarity.EPIC)
        addPet("MEGALODON", LorenzRarity.LEGENDARY, LorenzRarity.EPIC)
        addPet("PENGUIN", LorenzRarity.LEGENDARY)
        addPet("REINDEER", LorenzRarity.LEGENDARY, multiplier = 2.0)
        addPet("SPINOSAURUS", LorenzRarity.LEGENDARY)
        addPet("SQUID", LorenzRarity.LEGENDARY, LorenzRarity.EPIC)
        addPet("HERMIT_CRAB", LorenzRarity.MYTHIC, LorenzRarity.LEGENDARY, LorenzRarity.EPIC)
    }

    private fun MutableList<PetData>.addPet(name: String, vararg rarities: LorenzRarity, multiplier: Double = 1.0) {
        for (rarity in rarities) add(PetData(name, rarity, multiplier))
    }

    private class PetData(name: String, val rarity: LorenzRarity, val xpMultiplier: Double = 2.0) {
        private val level1 = "$name;${rarity.id}".toInternalName()
        private val level100 = "${level1.asString()}+100".toInternalName()
        val displayName: String get() = level1.repoItemName.removeSuffix(" Pet")

        fun getMaxXP() = when (rarity) {
            LorenzRarity.EPIC -> EPIC_MAX_XP
            LorenzRarity.LEGENDARY, LorenzRarity.MYTHIC -> LEG_MAX_XP
            else -> error("Max xp for rarity ${rarity.name} has not been defined yet")
        } / xpMultiplier

        fun getLowestBin() = level1.getPriceOrNull()
        fun getLowestBinLvl100() = level100.getPriceOrNull()
    }

    private data class PetProfit(
        val pet: PetData,
        val lowestBin: Double,
        val lowestBinLvl100: Double,
        val profit: Double,
        val pricePerXp: Double,
    )

    private fun calculatePrices() {
        val prices = pets.mapNotNull { pet ->
            val maxXp = pet.getMaxXP()
            val lbin = pet.getLowestBin() ?: return@mapNotNull null
            val lvl100 = pet.getLowestBinLvl100() ?: return@mapNotNull null
            val profit = lvl100 - lbin
            val pricePerXp = profit / maxXp
            PetProfit(pet, lbin, lvl100, profit, pricePerXp)
        }.sortedByDescending { it.pricePerXp }

        if (prices.isEmpty()) return
        val message = prices.joinToString("\n") { data ->
            "§7 - ${data.pet.displayName}§7: §a${data.profit.shortFormat()} " +
                "§e(${data.lowestBin.shortFormat()} -> ${data.lowestBinLvl100.shortFormat()}) " +
                "§6(${data.pricePerXp.roundTo(2).addSeparators()} per XP)"
        }
        NautilusChat.chat("Best Fishing Pet Profits:\n$message")
    }

    @HandleEvent
    fun onCommandRegistration(event: BrigadierRegisterEvent) {
        event.register("ntfishingpetprofit") {
            this.aliases = listOf("ntpetprofit", "ntbestpets")
            this.description = "Calculates the best fishing pet profits"
            this.category = CommandCategory.MAIN
            this.callback { calculatePrices() }
        }
    }

}
