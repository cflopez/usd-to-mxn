package com.credijusto.challenge.usdtomxn.vo

import com.credijusto.challenge.usdtomxn.serializers.InstantAsStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant


@Serializable
data class ProviderValue(
        @SerialName("last_updated")
        @Serializable(with = InstantAsStringSerializer::class)
        val lastUpdated: Instant = Instant.now(),
        val value: Double? = null,
        val source: String? = null,
        val valid: Boolean = false
    )