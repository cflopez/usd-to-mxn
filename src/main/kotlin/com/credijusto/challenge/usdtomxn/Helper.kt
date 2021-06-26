package com.credijusto.challenge.usdtomxn

import com.credijusto.challenge.usdtomxn.vo.ProviderValue
import java.time.Instant
import kotlin.math.pow
import kotlin.math.round

class Helper {
    companion object{
        fun roundRateValue(value: Double?, decimals: Int): Double {
            if (value != null) {
                val r = 10.0.pow(decimals.toDouble())
                return round(value * r) / r
            }
            return -1.0 //This return is not reached, however we are converting Double? to Double above
        }

        fun getProviderValueValidated(instant: Instant, value: Double?, source: String, decimals: Int): ProviderValue {
            if (value != null) {
                val rate = roundRateValue(value, decimals)
                if (rate >= 0) {
                    return ProviderValue(instant, rate, source,true)
                }
            }
            return ProviderValue(instant, -1.0, source,false)
        }
    }
}