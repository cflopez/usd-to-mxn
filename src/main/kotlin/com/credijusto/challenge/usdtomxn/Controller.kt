package com.credijusto.challenge.usdtomxn

import com.credijusto.challenge.usdtomxn.ratesources.banxico.BanxicoDomScraper
import com.credijusto.challenge.usdtomxn.ratesources.fixer.FixIoClient
import com.credijusto.challenge.usdtomxn.vo.ProviderValue
import com.credijusto.challenge.usdtomxn.vo.ResultValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Exception
import java.time.Instant

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce


@RestController
class Controller(private val appProperties: AppProperties) {

    @GetMapping("/priceRate_USD_to_MXN")
    fun getAll(): ResponseEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val getters = mapOf<String, (AppProperties) -> ResultValue>(
            appProperties.provider1 to { appProperties -> BanxicoDomScraper.getRates(appProperties) },
            appProperties.provider2 to { appProperties -> FixIoClient.getRates(appProperties) }
        )

        val result = getAllAsync(getters)

        return ResponseEntity.ok().headers(headers).body( Json.encodeToString(result) )
    }


    /**
     * Runs the process to get information from one provider.
     * If it fails, an empty ResultValue is returned with the valid status as false
     * and the default error message defined in app properties
     */
    private fun getOne( providerKey: String, getRates: (AppProperties) -> ResultValue ): ResultValue {
        return try {
            getRates(appProperties)

        } catch (e: Exception) {
            e.printStackTrace()
            val rates = mapOf( providerKey to
                    ProviderValue(
                        lastUpdated = Instant.now(),
                        source = "$providerKey: ${appProperties.unexpectedError}"
                    ))
            ResultValue(rates, listOf(e.javaClass.simpleName + ": " + e.message), emptyList())
        }
    }

    /**
     * Wrapper for getOne function on a Coroutine channel producer for async execution
     * of every service provider.
     *
     * If this channel producer fails because it is experimental, it can be replaced by
     * manually sending every getOne at runBlocking.launch and receiving every result
     * with runBlocking.receive instead of runBlocking.consumeEach.
     *
     * @param getters is a map with the providerKey string and the lambda
     * executor for each consumed service provider passed through getOne function.
     * @return The collection of results at the receive channel
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.getRateFunProducer(getters: Map<String, (AppProperties) -> ResultValue>)
            : ReceiveChannel<ResultValue> = produce {
        for ((providerKey, getFun) in getters) {
            send( getOne( providerKey ) { appProperties -> getFun(appProperties) } )
        }
    }

    /**
     * Async execution of all getter functions produced by getRateFunProducer.
     * After ll results are defined and merged the final result is ready to be returned.
     */
    private fun getAllAsync(getters: Map<String, (AppProperties) -> ResultValue>): ResultValue {
        return runBlocking {
            val rates = mutableMapOf<String, ProviderValue>()
            val errors = mutableListOf<String>()
            val warnings = mutableListOf<String>()

            val results = getRateFunProducer(getters)

            results.consumeEach {
                rates.putAll(it.rates)
                errors.addAll(it.errors)
                warnings.addAll(it.warnings)
            }

            ResultValue(rates, errors, warnings)
        }
    }
}