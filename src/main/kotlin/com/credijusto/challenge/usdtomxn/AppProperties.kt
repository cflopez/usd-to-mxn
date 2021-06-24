package com.credijusto.challenge.usdtomxn

import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.NumberFormat
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Min
import javax.validation.constraints.Pattern
import kotlin.properties.Delegates


@Component
@ConfigurationProperties("app")
@Validated
class AppProperties : BaseProperties() {
}

abstract class BaseProperties {

    @URL
    open lateinit var urlBanxico: String
    @Pattern(regexp=".*\\.renglonTituloColumnas$")
    open lateinit var columnNamesSelectorBanxico: String
    @Pattern(regexp="([^,]*,){3}([^,]*)")
    open lateinit var columnNamesBanxico: String
    @Pattern(regexp="^([0-9]+,){3}[0-9]+$")
    open lateinit var nameIndexesBanxico: String
    @Pattern(regexp="^([0-9]+,){3}[0-9]+$")
    open lateinit var valueIndexesBanxico: String
    @Pattern(regexp="((yyyy)|(MM)|(dd))[\\/-]((MM)|(dd))[\\/-]((yyyy)|(dd))")
    open lateinit var dateFormatBanxico: String
    @Pattern(regexp="([^,]*,){2}([^,]*)")
    open lateinit var variantsJsonBanxico: String
    @Min(1)
    open lateinit var roundDecimals: String
    //open var roundDecimals by Delegates.notNull<Int>()
}