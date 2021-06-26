package com.credijusto.challenge.usdtomxn.ratesources.fixer.vo


import com.credijusto.challenge.usdtomxn.serializers.InstantAsLongSerializer
import java.time.Instant
import kotlinx.serialization.Serializable


@Serializable
data class FixResponseValue (
        val success: Boolean,

        @Transient
        @Serializable(with = InstantAsLongSerializer::class)
        val timestamp: Instant? = null,

        @Transient
        val base: String? = null,

        @Transient
        val date: String? = null,

        @Transient
        val rates: Map<String, Double>? = null,

        @Transient
        val error: FixErrorValue? = null
    )