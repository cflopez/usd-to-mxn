package com.credijusto.challenge.usdtomxn.serializers

import com.credijusto.challenge.usdtomxn.ratesources.fixer.vo.enum.StatusCode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


object StatusCodeAsIntSerializer : KSerializer<StatusCode> {
    override fun deserialize(decoder: Decoder): StatusCode {
        return StatusCode.getFromValue(decoder.decodeInt())
    }
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StatusCodeInt", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: StatusCode) {
        encoder.encodeInt(value.code)
    }
}