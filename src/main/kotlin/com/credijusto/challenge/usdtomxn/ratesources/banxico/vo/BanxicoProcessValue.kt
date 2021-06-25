package com.credijusto.challenge.usdtomxn.vo

import com.credijusto.challenge.usdtomxn.ratesources.banxico.BanxicoDomScraper
import org.jsoup.select.Elements

class BanxicoProcessValue (
    val elms: Elements,
    val idxs: Map<BanxicoDomScraper.Companion.indexNames, Int>,
    val stringDateValue: String,
    val keyVariants: Array<String>,
    val decimals: Int
)