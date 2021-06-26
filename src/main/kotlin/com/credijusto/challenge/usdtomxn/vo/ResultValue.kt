package com.credijusto.challenge.usdtomxn.vo

import com.credijusto.challenge.usdtomxn.AppProperties
import kotlinx.serialization.Serializable


@Serializable
class ResultValue (val rates: Map<String, ProviderValue>,
                   val errors: List<String>,
                   val warnings: List<String>
    )