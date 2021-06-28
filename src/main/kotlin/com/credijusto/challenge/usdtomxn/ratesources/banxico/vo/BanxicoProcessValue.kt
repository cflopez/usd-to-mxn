package com.credijusto.challenge.usdtomxn.ratesources.banxico.vo

import com.credijusto.challenge.usdtomxn.ratesources.banxico.BanxicoDomScraper
import org.jsoup.select.Elements

class BanxicoProcessValue (
    val elms: Elements,
    val idxs: Map<BanxicoDomScraper.indexNames, Int>,
    val stringDateValue: String,
    val keyVariants: Array<String>,
    val decimals: Int
)