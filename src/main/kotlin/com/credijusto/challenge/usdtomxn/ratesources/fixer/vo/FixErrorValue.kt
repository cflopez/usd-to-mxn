package com.credijusto.challenge.usdtomxn.ratesources.fixer.vo

import com.credijusto.challenge.usdtomxn.ratesources.fixer.vo.enum.StatusCode
import com.credijusto.challenge.usdtomxn.serializers.StatusCodeAsIntSerializer
import kotlinx.serialization.Serializable

@Serializable
class FixErrorValue (
        @Serializable(with = StatusCodeAsIntSerializer::class)
        val code: StatusCode,
        val type: String,
        val info: String
    )