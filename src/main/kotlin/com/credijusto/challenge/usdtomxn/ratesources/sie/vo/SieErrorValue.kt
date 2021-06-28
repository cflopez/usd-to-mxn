package com.credijusto.challenge.usdtomxn.ratesources.sie.vo


import kotlinx.serialization.Serializable


@Serializable
class SieErrorValue (
        val url: String,
        val mensaje: String,
        val detalle: String
    )