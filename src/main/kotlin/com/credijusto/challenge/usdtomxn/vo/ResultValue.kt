package com.credijusto.challenge.usdtomxn.vo

import kotlinx.serialization.Serializable


@Serializable
class ResultValue (val rates: Map<String, ProviderValue>, val errors: List<String>)