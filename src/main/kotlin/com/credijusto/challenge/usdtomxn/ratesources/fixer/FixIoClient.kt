package com.credijusto.challenge.usdtomxn.ratesources.fixer

import com.credijusto.challenge.usdtomxn.AppProperties
import com.credijusto.challenge.usdtomxn.Helper
import com.credijusto.challenge.usdtomxn.Helper.Companion.getProviderValueValidated
import com.credijusto.challenge.usdtomxn.ratesources.fixer.vo.FixResponseValue
import com.credijusto.challenge.usdtomxn.vo.ProviderValue
import com.credijusto.challenge.usdtomxn.vo.ResultValue

import khttp.get
import khttp.responses.Response
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

import java.time.Instant


class FixIoClient (val appProperties: AppProperties) {
    
    val provider2 = Helper(appProperties).providers[1]

    private val logger = LoggerFactory.getLogger(javaClass)

    enum class accessType (val value: String) {
        FREE("FREE"),
        BASIC("BASIC"),
        PROFESSIONAL("PROFESSIONAL"),
        PROFESSIONAL_PLUS("PROFESSIONAL PLUS"),
        ENTERPRISE("ENTERPRISE")
    }

    private val source = "FIX.IO"

    /**
     * If your accessKey corresponds to a free plan, the strategy to get the rate
     * is going to be from EUR to USD to MXN due the limitations of the plan.
     */
    fun getRates(): ResultValue {
        if (appProperties.accessTypeFixIo.equals(accessType.FREE.value)) {
            val fixRes = getRateForFree()
            return if (fixRes.success) {
                val value = fixRes.rates?.get("MXN")!!?.div(fixRes.rates["USD"]!!)
                val decimals = Integer.parseInt(appProperties.roundDecimals)
                val rates =  mapOf( provider2 to
                        getProviderValueValidated(Instant.now(), value, source, decimals)
                )
                ResultValue(rates, emptyList(),
                    listOf(provider2
                        + ": This is a free plan, used a conversion strategy from EUR to USD to MXN.")
                )
            } else {
                buildErrorResponse(fixRes)
            }

        } else {
            val fixRes = getRateForPayed()
            return if (fixRes.success) {
                val decimals = Integer.parseInt(appProperties.roundDecimals)
                val rates =  mapOf( provider2 to
                        getProviderValueValidated(Instant.now(), fixRes.rates?.get("MXN"), "FIX.IO", decimals)
                )
                ResultValue(rates, emptyList(), emptyList())

            } else {
                buildErrorResponse(fixRes)
            }
        }
    }

    /**
     * Warning about Illegal reflective access
     * https://github.com/ascclemens/khttp/issues/29
     * TODO: Change the library or update khttp when new version available
     */
    private fun getRateForFree(): FixResponseValue {
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
    private fun getRateForPayed(): FixResponseValue {
        val response : Response = khttp.get(
            url = appProperties.urlFixIo,
            params = mapOf("access_key" to appProperties.accessKeyFixIo, "base" to "USD", "symbols" to "MXN")
        )
        val json = response.jsonObject.toString()
        logger.debug(json)
        return Json.decodeFromString(FixResponseValue.serializer(), json)
    }

    private fun buildErrorResponse(fixRes: FixResponseValue): ResultValue {
        val rates = mapOf( provider2 to
                ProviderValue(
                    lastUpdated = Instant.now(),
                    source = source
                ))
        return ResultValue(rates, listOf(provider2 + ": "
                + (fixRes.error?.code ?: 0) + " - " + (fixRes.error?.info ?: "")), emptyList())
    }

}