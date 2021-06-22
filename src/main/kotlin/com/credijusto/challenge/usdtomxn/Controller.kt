package com.credijusto.challenge.usdtomxn

import com.credijusto.challenge.usdtomxn.ratesources.banxico.BanxicoDomScraper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class Controller(private val appProperties: AppProperties) {

    @GetMapping("/banxicourl")
    fun getBanxicoUrl(): String {
        val table = BanxicoDomScraper.tableSelect(appProperties)
        if (table != null) {
            table[0].allElements.forEach {
                println(it.ownText())
            }
        }
        return appProperties.urlBanxico
    }
}