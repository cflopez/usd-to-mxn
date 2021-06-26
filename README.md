# USD to MXN API

### Credijusto Technical Challenge
##
Build a web service which exposes the current exchange rate of USD to MXN from three
different sources in the same endpoint. The response format should be something like the
following (feel free to adjust or modify this format):
###
    {
        "rates": {
            "provider_1": {
                "last_updated": "2018-04-22T18:25:43.511Z",
                "value": 20.4722
            },
            "provider_2_variant_1": {
                "last_updated": "2018-04-23T18:25:43.511Z",
                "value": 20.5281
            }
        }
    }
###
####<i>* Requirements *</i>

There are 3 different Exchange rate sources:

* [Diario Oficial de la Federación](https://www.banxico.org.mx/tipcamb/tipCamMIAction.do) - No API provided, so you will need to scrape the site
* [Fixer](https://fixer.io/) - Well documented API in JSON format
* [Banxico](https://www.banxico.org.mx/SieAPIRest/service/v1/doc/consultaDatosSerieRango) - Service SF43718. API returns XML.

###
This project was built using [Open JDK 13](http://jdk.java.net/java-se-ri/13)
on [JetBrains IntelliJ IDEA](https://www.jetbrains.com/es-es/idea/)

A stub was generated with [Spring Initializr](https://start.spring.io/) with the next options:
* Gradle Project
* Kotlin Language
* Spring Boot 2.5.1
* Jar Packaging
* Java 16 (gradle build file later edited for Java 13)
* No dependencies

###
The next JSON structure format was used as the Result to complement the requirement mentioned before:

    {
        "rates": {
            "provider_1": {
                "last_updated": "2018-04-22T18:25:43.511Z",
                "value": "20.4722",
                "source": "Para pagos",
                "valid": true
            },
            "provider_2_variant_1": {
                "last_updated": "2018-04-23T18:25:43.511Z",
                "value": "N/E",
                "source": "FIX",
                "valid": false
            }
        },
        "errors":[],
        "warnings":[]
    }

From the example above, the next things should be pointed:
* "source" tell us the origin with a meaningful text even if we already have a key parent like "provider_1".
* "value" number with n decimals (specified on app properties) if "valid" is true.
* If "valid" is false then "value" may have -1.0 and if possible the actual value returned
  from the source is going to be mentioned on a warning or error in JSON response.
* If errors found from any of the sites and sources, you will have them here as an array of strings.
* If warnings found from any of the sites and sources, you will have them here as an array of strings.

Every service call is exceuted in parallel using Kotlin Coroutines, so even if a service fails,
all others should be present with its rate or an error message.