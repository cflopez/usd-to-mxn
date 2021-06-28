package com.credijusto.challenge.usdtomxn

import com.credijusto.challenge.usdtomxn.ratesources.banxico.BanxicoDomScraper
import com.credijusto.challenge.usdtomxn.ratesources.fixer.FixIoClient
import com.credijusto.challenge.usdtomxn.ratesources.sie.SieApiClient
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
import kotlinx.coroutines.channels.produce
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@RestController
class Controller(val appProperties: AppProperties) {

    /**
     * As every requested service at the backend is executed asynchronously
     * with Coroutines, in order to identify which service did, we register
     * its providerKey. After all sent services were consumed,
     * if there was one or more that didn't, this set will help identifying
     * them and add in the final result those that timed-out.
     */
    var sentToChannel = mutableSetOf<String>()

    /**
     * Every providerKey that was successfully received will be added here.
     * All providerKeys that are not here but are in the senToChannel set
     * will be added to the final result as a time-out.
     * Lets take in consideration that these keys would also have variant suffixes.
     */
    var receivedFromChannel = mutableSetOf<String>()

    @GetMapping("/priceRate_USD_to_MXN")
    fun getAll(): ResponseEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val banxico = BanxicoDomScraper(appProperties)
        val fixio = FixIoClient(appProperties)
        val sieapi = SieApiClient(appProperties)

        val helper = Helper(appProperties)

        val getters = mapOf(
            banxico.provider1 to { banxico.getRates() },
            fixio.provider2 to { fixio.getRates() },
            sieapi.provider3 to { sieapi.getRates() }
        )

        val result = getAllAsync(helper.providers, getters)

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
    private fun CoroutineScope.getRateFunProducer(getters: Map<String, () -> ResultValue>)
            : ReceiveChannel<ResultValue> = produce {
        for ((providerKey, getFun) in getters) {
            send( getOne( providerKey ) { getFun() } )
            sentToChannel.add(providerKey)
        }
    }

    /**
     * Async execution of all getter functions produced by getRateFunProducer.zz
     * After ll results are defined and merged the final result is ready to be returned.
     */
    @OptIn(ExperimentalTime::class)
    private fun getAllAsync(
        providers: Array<String>,
        getters: Map<String, () -> ResultValue>): ResultValue {
        return runBlocking {
            val rates = mutableMapOf<String, ProviderValue>()
            val errors = mutableListOf<String>()
            val warnings = mutableListOf<String>()

            val results = getRateFunProducer(getters)

            //Replaced consumeEach to gain more grained control
            //and be able to consider a timeout. Let's keep
            //in consideration that the exact number of jobs in
            //the producer is known and every one should be handled.
            repeat(getters.size) {
                val res = withTimeoutOrNull(Duration.milliseconds(appProperties.timeoutForEachCall.toLong())) {
                    //receive is cancellable, so it suites fine for withTimeout block
                    results.receive()
                }
                if (res != null) {
                    rates.putAll(res.rates)
                    errors.addAll(res.errors)
                    warnings.addAll(res.warnings)

                    providers.forEach {
                        if (res.rates.toList()[0].first.contains(it)) {
                            receivedFromChannel.add(it)
                        }
                    }
                }
            }

            addTimeOutsToResult(rates, errors)
            ResultValue(rates, errors, warnings)
        }
    }

    /**
     * The relative complement of receivedFromChannel in sentToChannel
     * are the providerKeys that timed-out; that is, all the providerKeys
     * that are in sentToChannel but are not in receivedFromChannel
     */
    private fun addTimeOutsToResult(rates: MutableMap<String, ProviderValue>,
                                    errors: MutableList<String>) {
        sentToChannel.removeAll(receivedFromChannel)
        sentToChannel.forEach { timedOut ->
            rates.put(timedOut, ProviderValue())
            errors.add(timedOut + ": " + "Time out for this request.")
        }
    }
}