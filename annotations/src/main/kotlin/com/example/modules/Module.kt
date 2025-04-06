package com.example.modules

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Module(
    /**
     * If the module will only be loaded in a development environment.
     */
    val devOnly: Boolean = false,
)
