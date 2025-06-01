package com.github.itsempa.nautilus.config.core.loader

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class NullableStringTypeAdapter<T : Any>(
    val serializer: T.() -> String,
    val deserializer: String.() -> T?
) : TypeAdapter<T>() {

    override fun write(out: JsonWriter, value: T) {
        out.value(serializer(value))
    }

    override fun read(reader: JsonReader): T? = deserializer(reader.nextString())

}
