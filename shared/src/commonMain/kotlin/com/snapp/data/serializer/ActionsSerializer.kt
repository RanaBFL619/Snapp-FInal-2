package com.snapp.data.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

object ActionsSerializer : KSerializer<List<String>?> {
    override val descriptor = kotlinx.serialization.descriptors.buildClassSerialDescriptor("Actions")

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): List<String>? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        val element = jsonDecoder.decodeJsonElement()
        if (element !is JsonArray) return null
        return element.map { item ->
            when (item) {
                is JsonPrimitive -> item.content
                is JsonObject -> item["id"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
        }.filter { it.isNotBlank() }
    }

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: List<String>?) {
        val jsonEncoder = encoder as? JsonEncoder ?: return
        if (value == null) return
        val array = JsonArray(value.map { JsonPrimitive(it) })
        jsonEncoder.encodeJsonElement(array)
    }
}
