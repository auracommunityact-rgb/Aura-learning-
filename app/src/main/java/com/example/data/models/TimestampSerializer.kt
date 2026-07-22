package com.example.data.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimestampSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Timestamp", PrimitiveKind.STRING)

    private val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun deserialize(decoder: Decoder): Long {
        val isoString = decoder.decodeString()
        return try {
            df.parse(isoString)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    override fun serialize(encoder: Encoder, value: Long) {
        val isoString = df.format(Date(value))
        encoder.encodeString(isoString)
    }
}
