package com.credijusto.challenge.usdtomxn.ratesources.sie

import com.credijusto.challenge.usdtomxn.AppProperties
import com.credijusto.challenge.usdtomxn.Helper
import com.credijusto.challenge.usdtomxn.ratesources.sie.vo.SieResponseValue
import com.credijusto.challenge.usdtomxn.vo.ProviderValue
import com.credijusto.challenge.usdtomxn.vo.ResultValue
import khttp.responses.Response
import khttp.get
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Instant

class SieApiClient (val appProperties: AppProperties) {

    val provider3 = Helper(appProperties).providers[2]
    
    private val logger = LoggerFactory.getLogger(javaClass)

    private val sourcePrefix = "SIE API - "


    fun getRates(): ResultValue {
        val rates = mutableMapOf<String, ProviderValue>()
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        val variants = appProperties.variantsJsonSieApi.split(",").toTypedArray()

        val sieResponseValue = getRatesFrom(appProperties)
        val now = Instant.now()

        return if (sieResponseValue.bmx != null) {
            for (i in 0 until sieResponseValue.bmx.series.size) {
                val sieSerieValue = sieResponseValue.bmx.series[i]
                rates.put(provider3 + variants[i] , ProviderValue(
                    now, sieSerieValue.datos[0].dato.toDoubleOrNull(),
                    sourcePrefix + sieSerieValue.idSerie, true
                ))
                Helper.addError(sourcePrefix + sieSerieValue.idSerie, warnings,
                    "The last update for this rate type is " + sieSerieValue.datos[0].fecha)
            }
            ResultValue(rates, errors, warnings)

        } else {
            if (sieResponseValue.error != null) {
                Helper.addError(provider3, errors, sieResponseValue.error.mensaje)
            }
            ResultValue(rates, errors, warnings)
        }
    }

    private fun getRatesFrom(appProperties: AppProperties): SieResponseValue {
        val response : Response = get (
            url = appProperties.urlSieApi.replace(appProperties.urlSieApiPathVariable, appProperties.seriesSieApi),
            headers = mapOf("Bmx-Token" to appProperties.tokenSieApi),
        )
        val json = response.jsonObject.toString()
        logger.debug(json)
        return Json.decodeFromString(SieResponseValue.serializer(), json)
    }


}