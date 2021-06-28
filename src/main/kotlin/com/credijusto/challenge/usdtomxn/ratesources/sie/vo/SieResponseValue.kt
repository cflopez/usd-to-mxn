package com.credijusto.challenge.usdtomxn.ratesources.sie.vo


import kotlinx.serialization.Serializable


@Serializable
class SieResponseValue (
        @Transient
        val bmx: SieBmxValue? = null,
        @Transient
        val error: SieErrorValue? = null
    )
