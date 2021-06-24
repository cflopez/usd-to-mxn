package com.credijusto.challenge.usdtomxn.ratesources.banxico

import com.credijusto.challenge.usdtomxn.AppProperties
import org.jsoup.select.Elements
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.ThreadLocalRandom


@ExtendWith(SpringExtension::class)
@EnableConfigurationProperties(AppProperties::class)
@TestPropertySource("classpath:application.properties")
internal class BanxicoDomScraperTest {

    @Autowired
    private lateinit var appProperties: AppProperties

    /**
     * Should get the Elements from site
     */
    private var table: Elements? = null

    /**
     * Column Names should be contained in table
     */
    private lateinit var columnNames: Array<String>


    @BeforeEach
    private fun `intialize table and columnNames if null`() {
        if (table == null) {

            table = BanxicoDomScraper.tableSelect(appProperties)

            if (table != null && table!!.size > 0) {
                columnNames = appProperties.columnNamesBanxico.split(",").toTypedArray()
            }
        }
    }


    /**
     * If all words were found, count should equals columnNames.size
     */
    @Test
    internal fun `tableSelect Elements should be found from columnNames`() {
        var count = 0

        table!![0].allElements.forEach {
            if (columnNames.contains(it.ownText())) {
                count++
                println("MATCHED: " + it.ownText())
            }
        }

        println("tableSelect count: "+count)
        println("tableSelect columnNames.size: "+columnNames.size)

        assertTrue(count > 0 && count == columnNames.size)
    }


    /**
     * Similar to the previous test, but validating getIndexDefinitions
     */
    @Test
    internal fun `Indexes for column names should be found on map`() {
        val map = BanxicoDomScraper.getIndexDefinitions(appProperties)

        var index = 0
        var count = 0

        val listIndexNames = listOf<Int>(
            map[BanxicoDomScraper.Companion.indexNames.FECHA_NAME]!!,
            map[BanxicoDomScraper.Companion.indexNames.FIX_NAME]!!,
            map[BanxicoDomScraper.Companion.indexNames.DOF_NAME]!!,
            map[BanxicoDomScraper.Companion.indexNames.PAGOS_NAME]!!
        )

        table?.get(0)?.allElements?.forEach {
            if (columnNames.contains(it.ownText()) && listIndexNames.contains(index)) {
                count++
                println("" + index + ": VALID INDEX FOR: " + it.ownText())
            }
            index++
        }

        assertTrue(count > 0 && count == columnNames.size)
    }


    /**
     * To be successful at this test, the property appProperties.dateFormatBanxico
     * should be valid
     */
    @Test
    internal fun `Validates the method validateHTMLFoundDate for valid parameters`() {
        val now = Instant.now()
        val nowDateString = SimpleDateFormat(appProperties.dateFormatBanxico).format(Date.from( now ))
        assertTrue(
            BanxicoDomScraper.validateHTMLFoundDate(nowDateString, now, appProperties.dateFormatBanxico)
        )
    }

}