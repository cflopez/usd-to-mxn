package com.credijusto.challenge.usdtomxn.ratesources.banxico

import com.credijusto.challenge.usdtomxn.AppProperties
import com.credijusto.challenge.usdtomxn.Helper
import com.credijusto.challenge.usdtomxn.Helper.Companion.getProviderValueValidated
import com.credijusto.challenge.usdtomxn.ratesources.banxico.vo.BanxicoProcessValue
import com.credijusto.challenge.usdtomxn.vo.ProviderValue
import com.credijusto.challenge.usdtomxn.vo.ResultValue

import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.slf4j.LoggerFactory

import java.lang.IndexOutOfBoundsException
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*


class BanxicoDomScraper (val appProperties: AppProperties) {

    val provider1 = Helper(appProperties).providers[0]

    private val logger = LoggerFactory.getLogger(javaClass)

    enum class indexNames {
        FECHA_NAME, FECHA_VALUE, FIX_NAME, FIX_VALUE, DOF_NAME, DOF_VALUE, PAGOS_NAME, PAGOS_VALUE
    }


    /**
     * Return a map to make life easier when handling indexes inside Elements collection
     */
    fun getIndexDefinitions(): Map<indexNames, Int> {
        val nameIndexes = appProperties.nameIndexesBanxico.split(",").toTypedArray().map { it.toInt() }
        val valueIndexes = appProperties.valueIndexesBanxico.split(",").toTypedArray().map { it.toInt() }

        //Indexes were previously validated through a regexp. Both collections are expected to be of size 4
        return mapOf<indexNames, Int>(
            indexNames.FECHA_NAME to nameIndexes.get(0),
            indexNames.FECHA_VALUE to valueIndexes.get(0),
            indexNames.FIX_NAME to nameIndexes.get(1),
            indexNames.FIX_VALUE to valueIndexes.get(1),
            indexNames.DOF_NAME to nameIndexes.get(2),
            indexNames.DOF_VALUE to valueIndexes.get(2),
            indexNames.PAGOS_NAME to nameIndexes.get(3),
            indexNames.PAGOS_VALUE to valueIndexes.get(3),
        )
    }

    /**
     * Lets extract the information from the HTML table to scrape
     */
    fun tableSelect(): Elements? {
        val doc = Jsoup.connect(appProperties.urlBanxico).get()
        return doc.select(appProperties.columnNamesSelectorBanxico)
            .parents()
    }

    /**
     * A date is expected inside the HTML table and it should match with Now
     */
    fun validateHTMLFoundDate(foundDateString: String, now: Instant, dateFormat: String): Boolean {
        val nowDateString = SimpleDateFormat(dateFormat).format(Date.from( now ))
        return foundDateString == nowDateString
    }


    /**
     * So this is the main method to handle the process to scrape from the first provider
     * and get the results
     */
    @Throws(Exception::class)
    fun getRates(): ResultValue {
        val table = tableSelect()
        if (table != null) {

            //Current Instant of time
            val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
            var errors = mutableListOf<String>()

            var pv = validateAndScrape(table, now, errors)

            val res = buildResult(now, pv, errors)
            if (res != null) {
                return res
            }
        }

        throw Exception()
    }

    /**
     * Validating properties and preparing all information required for the process
     */
    private fun validateAndScrape(table: Elements?,
                                  now: Instant,
                                  errors: MutableList<String>
    ): BanxicoProcessValue? {

        val elms: Elements
        val idxs: Map<indexNames, Int>
        val stringDateValue: String
        val keyVariants: Array<String>
        val decimals: Int

        try {
            if (table != null && table.size > 0) {
                idxs = getIndexDefinitions()
                elms = table[0].allElements
                stringDateValue = elms[ idxs[indexNames.FECHA_VALUE]!! ].ownText()
                keyVariants = appProperties.variantsJsonBanxico.split(",").toTypedArray()
                decimals = Integer.parseInt(appProperties.roundDecimals)

                if (validateHTMLFoundDate(stringDateValue, now, appProperties.dateFormatBanxico)) {
                    return BanxicoProcessValue(elms, idxs, stringDateValue, keyVariants, decimals)

                } else {
                    Helper.addError(provider1, errors,
                        "The date found on the HTML table at Banxico site does not match with current. Please try again.")
                }

            } else {
                Helper.addError(provider1, errors,
                    "No elements found at defined site, nothing to do.")
            }

        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            /**
             * The error messages in this catch block are shown to the final user, but can just be logged
             * if the application.properties are final and verified by the provider.
             */

            when(ex) {
                is IndexOutOfBoundsException, is ArrayStoreException -> {
                    Helper.addError(provider1, errors,
                        "Index Definitions incorrectly defined at application.properties for first site.")
                }
                is NullPointerException -> {
                    Helper.addError(provider1, errors,
                        appProperties.unexpectedError)
                }
                is NumberFormatException -> {
                    Helper.addError(provider1, errors,
                        "roundDecimals incorrectly defined at application.properties.")
                }
                else -> throw ex
            }
        }

        Helper.addError(provider1, errors,
            appProperties.unexpectedError)

        return null
    }

    /**
     * Once all properties were validated and we scraped the site, lets build the result
     */
    private fun buildResult(now: Instant,
                            pv: BanxicoProcessValue?,
                            errors: MutableList<String>
    ): ResultValue? {

        var rates = mutableMapOf<String, ProviderValue>()
        var warnings = mutableListOf<String>()

        if (pv != null) {
            addProviderValue(pv, indexNames.FIX_NAME, indexNames.FIX_VALUE,
                rates, warnings, 0, now)
            addProviderValue(pv, indexNames.DOF_NAME, indexNames.DOF_VALUE,
                rates, warnings, 1, now)
            addProviderValue(pv, indexNames.PAGOS_NAME, indexNames.PAGOS_VALUE,
                rates, warnings, 2, now)

            return ResultValue(rates, errors, warnings)
        }

        return null
    }

    private fun addProviderValue(pv: BanxicoProcessValue,
                                 indexName: indexNames,
                                 indexValue: indexNames,
                                 rates: MutableMap<String, ProviderValue>,
                                 warnings: MutableList<String>,
                                 variantId: Int,
                                 now: Instant) {
        val src = appProperties.urlBanxico + " - " + pv.elms[ pv.idxs[indexName]!! ].ownText()
        val v = pv.elms[ pv.idxs[indexValue]!! ].ownText()
        val dVal = v.toDoubleOrNull()
        if (dVal == null) {
            Helper.addError(provider1 + pv.keyVariants[variantId], warnings,
                "Value in response from this provider is not a valid number, found: $v"
            )
        }
        rates[provider1 + pv.keyVariants[variantId]] =
            getProviderValueValidated(now, dVal, src, pv.decimals)
    }

}