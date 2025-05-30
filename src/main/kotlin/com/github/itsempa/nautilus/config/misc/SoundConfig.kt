package com.github.itsempa.nautilus.config.misc

import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorBoolean
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorButton
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorSlider
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigEditorText
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigOption
import at.hannibal2.skyhanni.deps.moulconfig.observer.Property
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.KSerializable
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.utils.NautilusUtils.asProperty
import com.google.gson.annotations.Expose
import kotlinx.coroutines.delay
import net.minecraft.client.audio.ISound
import kotlin.time.Duration.Companion.milliseconds

@KSerializable
data class SoundConfig(
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable playing the sound.")
    @ConfigEditorBoolean
    var enabled: Boolean = false,

    @Expose
    @ConfigOption(name = "Sound Name", desc = "Name of the sound to use")
    @ConfigEditorText
    val soundName: Property<String> = "note.pling".asProperty(),

    @Expose
    @ConfigOption(name = "Volume", desc = "Volume of sound")
    @ConfigEditorSlider(minValue = 0f, minStep = 0.1f, maxValue = 100f)
    val volume: Property<Float> = 50f.asProperty(),

    @Expose
    @ConfigOption(name = "Pitch", desc = "Pitch of the sound")
    @ConfigEditorSlider(minValue = 0f, minStep = 0.1f, maxValue = 2f)
    val pitch: Property<Float> = 1f.asProperty(),

    @Expose
    @ConfigOption(name = "Repeat Sound", desc = "Play the sound this amount of times every time it is played.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 10f, minStep = 1f)
    var repeat: Int = 1,

    @Expose
    @ConfigOption(name = "Repeat Duration", desc = "Amount of milliseconds between each sound played, in milliseconds.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 500f, minStep = 1f)
    var repeatDuration: Int = 50,
) {
    @Suppress("unused")
    constructor(
        enabled: Boolean = true,
        soundName: String = "note.pling",
        volume: Float = 50f,
        pitch: Float = 1f,
        repeat: Int = 1,
        repeatDuration: Int = 50,
    ) : this(enabled, soundName.asProperty(), volume.asProperty(), pitch.asProperty(), repeat, repeatDuration)


    @Transient
    @ConfigOption(name = "Test Sound", desc = "Test the sound")
    @ConfigEditorButton(buttonText = "Test")
    private val testSound = Runnable { playSound(true) }

    @Suppress("MemberVisibilityCanBePrivate")
    @Transient
    var sound: ISound = createSound()
        private set

    private fun createSound(): ISound = SoundUtils.createSound(soundName.get(), pitch.get(), volume.get())

    init {
        ConditionalUtils.onToggle(soundName, volume, pitch) {
            sound = createSound()
        }
    }

    fun playSound(bypass: Boolean = false) {
        if (!bypass && !enabled) return
        if (repeat <= 1) sound.playSound()
        else {
            val duration = repeatDuration.milliseconds
            Nautilus.launchCoroutine {
                repeat(repeat) {
                    sound.playSound()
                    delay(duration)
                }
            }
        }
    }
}
