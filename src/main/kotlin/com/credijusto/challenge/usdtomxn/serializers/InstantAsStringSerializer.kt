package com.credijusto.challenge.usdtomxn.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


object InstantAsStringSerializer : KSerializer<Instant> {
    override fun deserialize(decoder: Decoder): Instant {
        return Instant.ofEpochMilli(decoder.decodeString().toLong())
    }
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("InstantString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.from(ZoneOffset.UTC)).format(value)
        )
    }
}
