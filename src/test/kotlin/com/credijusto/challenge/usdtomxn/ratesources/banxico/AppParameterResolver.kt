package com.credijusto.challenge.usdtomxn.ratesources.banxico

import com.credijusto.challenge.usdtomxn.AppProperties
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.ParameterResolver


class AppParameterResolver : ParameterResolver {
    @Throws(ParameterResolutionException::class)
    override fun supportsParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?
    ): Boolean {
        return parameterContext?.parameter?.type == AppProperties::class.java
    }

    @Throws(ParameterResolutionException::class)
    override fun resolveParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?
    ): Any? {
        return AppProperties()
    }
}