package com.credijusto.challenge.usdtomxn


import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Min
import javax.validation.constraints.Pattern


@Component
@ConfigurationProperties(prefix = "app")
@Validated
class AppProperties : BaseProperties() {
}

abstract class BaseProperties {

    /**
     * Sensitive information as accessKey and token are handled via
     * spring-dotenv and dotenv-kotlin
     */

    open lateinit var accessKeyFixIo: String
    open lateinit var tokenSieApi: String


    @Min(1)
    open lateinit var roundDecimals: String

    open lateinit var unexpectedError: String

    @Pattern(regexp="[0-9]+")
    open lateinit var timeoutForEachCall: String

    @Pattern(regexp="^([^,\\s]*,){2}([^,\\s]*)$")
    open lateinit var providers: String


    @URL
    open lateinit var urlBanxico: String
    @Pattern(regexp=".*\\.renglonTituloColumnas$")
    open lateinit var columnNamesSelectorBanxico: String
    @Pattern(regexp="^([^,]*,){3}([^,]*)$")
    open lateinit var columnNamesBanxico: String
    @Pattern(regexp="^([0-9]+,){3}[0-9]+$")
    open lateinit var nameIndexesBanxico: String
    @Pattern(regexp="^([0-9]+,){3}[0-9]+$")
    open lateinit var valueIndexesBanxico: String
    @Pattern(regexp="^((yyyy)|(MM)|(dd))[/-]((MM)|(dd))[/-]((yyyy)|(dd))$")
    open lateinit var dateFormatBanxico: String
    @Pattern(regexp="^([^,\\s]*,){2}([^,\\s]*)$")
    open lateinit var variantsJsonBanxico: String

    @URL
    open lateinit var urlFixIo: String

    @Pattern(regexp="^(FREE|BASIC|PROFESSIONAL|PROFESSIONAL PLUS|ENTERPRISE)$")
    open lateinit var accessTypeFixIo: String

    @URL
    open lateinit var urlSieApi: String
    @Pattern(regexp="^\\{([^{},\\s])*}$")
    open lateinit var urlSieApiPathVariable: String
    @Pattern(regexp="^([^,\\s]*,)*([^,\\s]*)$")
    open lateinit var seriesSieApi: String

    @Pattern(regexp="^([^,\\s]*,)*([^,\\s]*)$")
    open lateinit var variantsJsonSieApi: String
}