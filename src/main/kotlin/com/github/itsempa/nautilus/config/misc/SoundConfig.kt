package com.github.itsempa.nautilus.config.misc

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorButton
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorSlider
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorText
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import at.hannibal2.skyhanni.deps.moulconfig.observer.Property
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusUtils.asProperty
import com.google.gson.annotations.Expose
import net.minecraft.client.audio.ISound

@Suppress("CanBePrimaryConstructorProperty")
class SoundConfig(
    enabled: Boolean = true,
    soundName: String = "note.pling",
    volume: Float = 50f,
    pitch: Float = 1f,
    repeat: Int = 1,
) {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable playing the sound.")
    @ConfigEditorBoolean
    private var enabled: Boolean = enabled

    @Expose
    @ConfigOption(name = "Sound Name", desc = "Name of the sound to use")
    @ConfigEditorText
    private val soundName: Property<String> = soundName.asProperty()

    @Expose
    @ConfigOption(name = "Volume", desc = "Volume of sound")
    @ConfigEditorSlider(minValue = 0f, minStep = 0.1f, maxValue = 100f)
    private val volume: Property<Float> = volume.asProperty()

    @Expose
    @ConfigOption(name = "Pitch", desc = "Pitch of the sound")
    @ConfigEditorSlider(minValue = 0f, minStep = 0.1f, maxValue = 2f)
    private val pitch: Property<Float> = pitch.asProperty()

    @Expose
    @ConfigOption(name = "Repeat Sound", desc = "Play the sound this amount of times every time it is played.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 10f, minStep = 1f)
    private val repeat: Int = repeat

    @Transient
    @ConfigOption(name = "Test Sound", desc = "Test the sound")
    @ConfigEditorButton(buttonText = "Test")
    private val testSound = Runnable { playSound(true) }

    private fun updateSound() {
        sound = SoundUtils.createSound(soundName.get(), pitch.get(), volume.get())
    }

    @Suppress("MemberVisibilityCanBePrivate")
    @Transient
    lateinit var sound: ISound
        private set

    init {
        list.add(this)
    }

    fun playSound(bypass: Boolean = false) {
        if (bypass || enabled) repeat(repeat) { sound.playSound() }
    }

    @Module
    companion object {
        private val list = mutableListOf<SoundConfig>()

        // TODO: see if this can be done without the config load event
        @HandleEvent
        fun onConfigLoad(event: ConfigLoadEvent) {
            list.forEach { config ->
                with(config) {
                    updateSound()
                    ConditionalUtils.onToggle(soundName, volume, pitch) {
                        updateSound()
                    }
                }
            }
            list.clear() // clear list as we dont need it anymore
        }

    }
}
