package com.credijusto.challenge.usdtomxn

import com.credijusto.challenge.usdtomxn.ratesources.banxico.BanxicoDomScraper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class Controller(private val appProperties: AppProperties) {

    @GetMapping("/priceRate_USD_to_MXN")
    fun getBanxicoScraped(): ResponseEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val result = BanxicoDomScraper.getValuesFromTable(appProperties)
        return ResponseEntity.ok().headers(headers).body( Json.encodeToString(result) )
    }
}