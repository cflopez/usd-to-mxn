package com.credijusto.challenge.usdtomxn.ratesources.fixer

import com.credijusto.challenge.usdtomxn.AppProperties
import com.credijusto.challenge.usdtomxn.Helper.Companion.getProviderValueValidated
import com.credijusto.challenge.usdtomxn.ratesources.fixer.vo.FixResponseValue
import com.credijusto.challenge.usdtomxn.serializers.JSONable
import com.credijusto.challenge.usdtomxn.vo.ProviderValue
import com.credijusto.challenge.usdtomxn.vo.ResultValue
import khttp.responses.Response
import org.json.JSONObject
import java.time.Instant


class FixIoClient {
    companion object {
        enum class accessType (val value: String) {
            FREE("FREE"),
            BASIC("BASIC"),
            PROFESSIONAL("PROFESSIONAL"),
            PROFESSIONAL_PLUS("PROFESSIONAL PLUS"),
            ENTERPRISE("ENTERPRISE")
        }

        private const val errorPrefix = "Provider2: "

        private const val source = "FIX.IO"

        fun getRateFromEndpoint(appProperties: AppProperties): ResultValue {
            if (appProperties.accessTypeFixIo.equals(accessType.FREE.value)) {
                val fixRes = getRateForFree(appProperties)
                if (fixRes.success) {
                    val value = fixRes.rates["MXN"]?.div(fixRes.rates["USD"]!!)
                    val decimals = Integer.parseInt(appProperties.roundDecimals)
                    val rates =  mapOf( appProperties.provider2Key to
                        getProviderValueValidated(Instant.now(), value, source, decimals)
                    )
                    return ResultValue(rates, emptyList(), listOf(errorPrefix + "This is a free plan, used a conversion strategy from EUR to USD to MXN."))

                } else {
                    return buildErrorResponse(appProperties, fixRes)
                }

            } else {
                val fixRes = getRateForPayed(appProperties)
                if (fixRes.success) {
                    val decimals = Integer.parseInt(appProperties.roundDecimals)
                    val rates =  mapOf( appProperties.provider2Key to
                            getProviderValueValidated(Instant.now(), fixRes.rates["MXN"], "FIX.IO", decimals)
                    )
                    return ResultValue(rates, emptyList(), emptyList())

                } else {
                    return buildErrorResponse(appProperties, fixRes)
                }
            }
        }

        fun getRateForFree(appProperties: AppProperties): FixResponseValue {
            val response : Response = khttp.get(
                url = appProperties.urlFixIo.replace("https:","http:",true),
                params = mapOf("access_key" to appProperties.accessKeyFixIo, "base" to "EUR", "symbols" to "USD,MXN")
            )
            val obj : JSONObject = response.jsonObject
            return JSONable.fromJSON(obj) as FixResponseValue
        }

        fun getRateForPayed(appProperties: AppProperties): FixResponseValue {
            val response : Response = khttp.get(
                url = appProperties.urlFixIo,
                params = mapOf("access_key" to appProperties.accessKeyFixIo, "base" to "USD", "symbols" to "MXN")
            )
            val obj : JSONObject = response.jsonObject
            return JSONable.fromJSON(obj) as FixResponseValue
        }

        fun buildErrorResponse(appProperties: AppProperties, fixRes: FixResponseValue): ResultValue {
            val rates = mapOf( appProperties.provider2Key to
                    ProviderValue(
                        lastUpdated = Instant.now(),
                        source = source
                    ))
            return ResultValue(rates, listOf(errorPrefix + fixRes.error.code + " - " + fixRes.error.info), emptyList())
        }

    }
}