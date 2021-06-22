package com.credijusto.challenge.usdtomxn.ratesources.banxico

import com.credijusto.challenge.usdtomxn.AppProperties
import org.jsoup.Jsoup;
import org.jsoup.select.Elements

class BanxicoDomScraper {

    companion object {
        fun tableSelect(appProperties: AppProperties): Elements? {
            val doc = Jsoup.connect(appProperties.urlBanxico).get()
            return doc.select(appProperties.renglonTituloColumnasSelectorBanxico)
                .parents()
        }
    }

}