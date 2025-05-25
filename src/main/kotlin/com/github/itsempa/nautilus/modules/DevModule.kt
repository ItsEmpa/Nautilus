package com.github.itsempa.nautilus.modules

import me.owdding.ktmodules.AutoCollect

@AutoCollect("DevModules")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DevModule
