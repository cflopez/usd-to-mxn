package com.credijusto.challenge.usdtomxn.vo

import com.credijusto.challenge.usdtomxn.serializers.InstantAsStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant


@Serializable
data class ProviderValue(
        @SerialName("last_updated")
        @Serializable(with = InstantAsStringSerializer::class)
        val lastUpdated: Instant,
        val value: Double = -1.0,
        val source: String,
        val valid: Boolean = false
    )