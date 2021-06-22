package com.credijusto.challenge.usdtomxn.ratesources.banxico

import com.credijusto.challenge.usdtomxn.AppProperties
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension::class)
@EnableConfigurationProperties(AppProperties::class)
@TestPropertySource("classpath:application.properties")
internal class BanxicoDomScraperTest {

    @Autowired
    private lateinit var appProperties: AppProperties

    @Test
    internal fun testTableSelect() {
        val table = BanxicoDomScraper.tableSelect(appProperties)

        if (table != null && table.size > 0) {

            // This strings should be contained in tableSelect result
            val words = arrayOf("Fecha", "FIX", "Publicación DOF", "Para pagos")
            var counts = 0

            table[0].allElements.forEach {
                if (words.contains(it.ownText())) counts++
            }

            // If all words were found, counts equals words.size
            assertTrue(counts == words.size)
        }
        else {
            // Must not be null and must have at least one iterable for getAllElements
            assertTrue(false)
        }
    }
}