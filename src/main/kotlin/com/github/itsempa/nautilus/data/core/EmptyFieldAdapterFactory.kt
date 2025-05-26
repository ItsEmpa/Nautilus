package com.github.itsempa.nautilus.data.core

import com.github.itsempa.nautilus.utils.NautilusNullableUtils.cast
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation

/**
 * Serializes a field as null if its empty.
 * (Accepts [Map], [Collection] and [String])
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class NullIfEmpty

// TODO: make sure this actually works
object EmptyFieldAdapterFactory : TypeAdapterFactory {

    override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val clazz = type.rawType

        @Suppress("UNCHECKED_CAST")
        val kotlinClass = clazz.kotlin as KClass<T>
        if (!kotlinClass.hasAnnotation<NullIfEmpty>()) return null
        val emptyFunction = clazz.getEmptyFunction() ?: return null
        val createInstanceFunction = kotlinClass.getEmptyInstanceFunction()

        val delegate = gson.getDelegateAdapter(this, type)

        return object : TypeAdapter<T>() {
            private val delegate = delegate
            private val emptyFunction = emptyFunction
            private val createInstanceFunction = createInstanceFunction
            override fun write(out: JsonWriter, value: T?) {
                if (value == null || this.emptyFunction(value)) {
                    out.nullValue()
                } else {
                    this.delegate.write(out, value)
                }
            }

            override fun read(input: JsonReader): T {
                if (input.peek() == JsonToken.NULL) {
                    input.nextNull()
                    return this.createInstanceFunction.callBy(emptyMap()) // Return an empty instance instead of null
                }
                return this.delegate.read(input)
            }
        }
    }

    private fun <T : Any> Class<T>.getEmptyFunction(): ((T) -> Boolean)? {
        return when {
            Collection::class.java.isAssignableFrom(this) -> {
                { cast<Collection<*>>().isEmpty() }
            }
            Map::class.java.isAssignableFrom(this) -> {
                { cast<Map<*, *>>().isEmpty() }
            }
            String::class.java.isAssignableFrom(this) -> {
                { cast<String>().isEmpty() }
            }
            else -> null
        }
    }

    private fun <T : Any> KClass<T>.getEmptyInstanceFunction(): KFunction<T> {
        return constructors.find {
            it.parameters.isEmpty() || it.parameters.all { param -> param.isOptional || param.type.isMarkedNullable }
        } ?: throw IllegalArgumentException("No suitable constructor found for class $qualifiedName")
    }
}
