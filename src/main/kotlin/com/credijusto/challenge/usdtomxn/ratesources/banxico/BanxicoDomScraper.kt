package com.credijusto.challenge.usdtomxn.ratesources.banxico

import com.credijusto.challenge.usdtomxn.AppProperties
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


class BanxicoDomScraper {

    companion object {

        private val logger = LoggerFactory.getLogger(javaClass)


        enum class indexNames {
            FECHA_NAME, FECHA_VALUE, FIX_NAME, FIX_VALUE, DOF_NAME, DOF_VALUE, PAGOS_NAME, PAGOS_VALUE
        }

        /**
         * To prefix every error we are going to show to the final user from this class
         */
        private fun addError(errorPrefix:String, errors: MutableList<String>, msg: String) {
            errors.add(errorPrefix + ": " + msg)
        }

        /**
         * Return a map to make life easier when handling indexes inside Elements collection
         */
        fun getIndexDefinitions(appProperties: AppProperties): Map<indexNames, Int> {
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
        fun tableSelect(appProperties: AppProperties): Elements? {
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
        fun getRates(appProperties: AppProperties): ResultValue {
            val table = tableSelect(appProperties)
            if (table != null) {

                //Current Instant of time
                val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
                var errors = mutableListOf<String>()

                var pv = validateAndScrape(table, appProperties, now, errors)

                return buildResult(now, pv, errors, appProperties) ?: throw Exception()
            }

            throw Exception()
        }

        /**
         * Validating properties and preparing all information required for the process
         */
        private fun validateAndScrape(table: Elements?,
                                      appProperties: AppProperties,
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
                    idxs = getIndexDefinitions(appProperties)
                    elms = table[0].allElements
                    stringDateValue = elms[ idxs[indexNames.FECHA_VALUE]!! ].ownText()
                    keyVariants = appProperties.variantsJsonBanxico.split(",").toTypedArray()
                    decimals = Integer.parseInt(appProperties.roundDecimals)

                    if (validateHTMLFoundDate(stringDateValue, now, appProperties.dateFormatBanxico)) {
                        return BanxicoProcessValue(elms, idxs, stringDateValue, keyVariants, decimals)

                    } else {
                        addError(appProperties.provider1, errors,
                            "The date found on the HTML table at Banxico site does not match with current. Please try again.")
                    }

                } else {
                    addError(appProperties.provider1, errors,
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
                        addError(appProperties.provider1, errors,
                            "Index Definitions incorrectly defined at application.properties for first site.")
                    }
                    is NullPointerException -> {
                        addError(appProperties.provider1, errors,
                            appProperties.unexpectedError)
                    }
                    is NumberFormatException -> {
                        addError(appProperties.provider1, errors,
                            "roundDecimals incorrectly defined at application.properties.")
                    }
                    else -> throw ex
                }
            }

            addError(appProperties.provider1, errors,
                appProperties.unexpectedError)

            return null
        }

        /**
         * Once all properties were validated and we scraped the site, lets build the result
         */
        private fun buildResult(now: Instant,
                                pv: BanxicoProcessValue?,
                                errors: MutableList<String>,
                                appProperties: AppProperties
        ): ResultValue? {

            var rates = mutableMapOf<String, ProviderValue>()
            var warnings = mutableListOf<String>()

            if (pv != null) {
                val fixSrc = pv.elms[ pv.idxs[indexNames.FIX_NAME]!! ].ownText()
                val fixVal = pv.elms[ pv.idxs[indexNames.FIX_VALUE]!! ].ownText()
                val dfixVal = fixVal.toDoubleOrNull()
                if (dfixVal == null) {
                    addError(appProperties.provider1, warnings,
                        "Value in response from this provider were not a valid number, found: " + fixVal)
                }
                rates.put(pv.keyVariants[0], getProviderValueValidated(now, dfixVal, fixSrc, pv.decimals))

                val dofSrc = pv.elms[ pv.idxs[indexNames.DOF_NAME]!! ].ownText()
                val dofVal = pv.elms[ pv.idxs[indexNames.DOF_VALUE]!! ].ownText()
                val ddofVal = dofVal.toDoubleOrNull()
                if (ddofVal == null) {
                    addError(appProperties.provider1, warnings,
                        "Value in response from this provider were not a valid number, found: " + dofVal)
                }
                rates.put(pv.keyVariants[1], getProviderValueValidated(now, ddofVal, dofSrc, pv.decimals))

                val pagosSrc = pv.elms[ pv.idxs[indexNames.PAGOS_NAME]!! ].ownText()
                val pagosVal = pv.elms[ pv.idxs[indexNames.PAGOS_VALUE]!! ].ownText()
                val dpagosVal = pagosVal.toDoubleOrNull()
                if (dpagosVal == null) {
                    addError(appProperties.provider1, warnings,
                        "Value in response from this provider were not a valid number, found: " + pagosVal)
                }
                rates.put(pv.keyVariants[2], getProviderValueValidated(now, dpagosVal, pagosSrc, pv.decimals))

                return ResultValue(rates, errors, warnings)
            }

            return null
        }
    }
}