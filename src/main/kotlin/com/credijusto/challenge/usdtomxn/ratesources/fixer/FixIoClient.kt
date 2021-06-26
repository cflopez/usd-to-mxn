package com.credijusto.challenge.usdtomxn.ratesources.fixer

import com.credijusto.challenge.usdtomxn.AppProperties
import com.credijusto.challenge.usdtomxn.Helper.Companion.getProviderValueValidated
import com.credijusto.challenge.usdtomxn.ratesources.fixer.vo.FixResponseValue
import com.credijusto.challenge.usdtomxn.vo.ProviderValue
import com.credijusto.challenge.usdtomxn.vo.ResultValue

import khttp.get
import khttp.responses.Response
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

import java.time.Instant


class FixIoClient {
    companion object {

        private val logger = LoggerFactory.getLogger(javaClass)

        enum class accessType (val value: String) {
            FREE("FREE"),
            BASIC("BASIC"),
            PROFESSIONAL("PROFESSIONAL"),
            PROFESSIONAL_PLUS("PROFESSIONAL PLUS"),
            ENTERPRISE("ENTERPRISE")
        }

        private const val source = "FIX.IO"

        fun getRates(appProperties: AppProperties): ResultValue {
            if (appProperties.accessTypeFixIo.equals(accessType.FREE.value)) {
                val fixRes = getRateForFree(appProperties)
                return if (fixRes.success) {
                    val value = fixRes.rates?.get("MXN")!!?.div(fixRes.rates["USD"]!!)
                    val decimals = Integer.parseInt(appProperties.roundDecimals)
                    val rates =  mapOf( appProperties.provider2 to
                            getProviderValueValidated(Instant.now(), value, source, decimals)
                    )
                    ResultValue(rates, emptyList(), listOf(appProperties.provider2 + ": This is a free plan, used a conversion strategy from EUR to USD to MXN."))

                } else {
                    buildErrorResponse(appProperties, fixRes)
                }

            } else {
                val fixRes = getRateForPayed(appProperties)
                return if (fixRes.success) {
                    val decimals = Integer.parseInt(appProperties.roundDecimals)
                    val rates =  mapOf( appProperties.provider2 to
                            getProviderValueValidated(Instant.now(), fixRes.rates?.get("MXN"), "FIX.IO", decimals)
                    )
                    ResultValue(rates, emptyList(), emptyList())

                } else {
                    buildErrorResponse(appProperties, fixRes)
                }
            }
        }


        /**
         * Warning about Illegal reflective access
         * https://github.com/ascclemens/khttp/issues/29
         * TODO: Change the library or update khttp when new version available
         */
        private fun getRateForFree(appProperties: AppProperties): FixResponseValue {
            val response : Response = get(
                url = appProperties.urlFixIo.replace("https:","http:",true),
                params = mapOf("access_key" to appProperties.accessKeyFixIo, "base" to "EUR", "symbols" to "USD,MXN")
            )
            val json = response.jsonObject.toString()
            logger.debug(json)
            return Json.decodeFromString(FixResponseValue.serializer(), json)
        }

        /**
         * Warning about Illegal reflective access
         * https://github.com/ascclemens/khttp/issues/29
         * TODO: Change the library or update khttp when new version available
         */
        private fun getRateForPayed(appProperties: AppProperties): FixResponseValue {
            val response : Response = khttp.get(
                url = appProperties.urlFixIo,
                params = mapOf("access_key" to appProperties.accessKeyFixIo, "base" to "USD", "symbols" to "MXN")
            )
            val json = response.jsonObject.toString()
            logger.debug(json)
            return Json.decodeFromString(FixResponseValue.serializer(), json)
        }

        private fun buildErrorResponse(appProperties: AppProperties, fixRes: FixResponseValue): ResultValue {
            val rates = mapOf( appProperties.provider2 to
                    ProviderValue(
                        lastUpdated = Instant.now(),
                        source = source
                    ))
            return ResultValue(rates, listOf(appProperties.provider2 + ": "
                    + (fixRes.error?.code ?: 0) + " - " + (fixRes.error?.info ?: "")), emptyList())
        }

    }
}