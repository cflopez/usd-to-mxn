package com.credijusto.challenge.usdtomxn

import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Pattern


@Component
@ConfigurationProperties("app")
@Validated
class AppProperties : BaseProperties() {
}

abstract class BaseProperties {

    @Pattern(regexp=".*\\.renglonTituloColumnas.*")
    open lateinit var renglonTituloColumnasSelectorBanxico: String

    @URL
    open lateinit var urlBanxico: String
}