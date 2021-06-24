package com.credijusto.challenge.usdtomxn.ratesources.banxico

import kotlin.reflect.KFunction
import kotlin.reflect.full.functions

class MethodResolver {
    companion object{
        fun callPrivate(objectInstance: Any, methodName: String, vararg args: Any?): Any? {
            val privateMethod: KFunction<*>? =
                objectInstance::class.functions.find { t -> return@find t.name == methodName }

            val argList = args.toMutableList()
            (argList as ArrayList).add(0, objectInstance)
            val argArr = argList.toArray()

            privateMethod?.apply {
                return call(*argArr)
            }
                ?: throw NoSuchMethodException("Method $methodName does not exist in ${objectInstance::class.qualifiedName}")
            return null
        }
    }
}
