package com.credijusto.challenge.usdtomxn.ratesources.sie.vo


import kotlinx.serialization.Serializable


@Serializable
class SieBmxValue (
        val series: List<SieSerieValue>,
    )
{
        @Serializable
        class SieSerieValue (
                val idSerie: String,
                val titulo: String,
                val datos: List<SieDatosValue>
            )
        {
            @Serializable
            class SieDatosValue (
                    val fecha: String,
                    val dato: String
                )
        }
}