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

####<i>* Requirements *</i>

There are 3 different Exchange rate sources:

* [Diario Oficial de la Federación](https://www.banxico.org.mx/tipcamb/tipCamMIAction.do) - No API provided, so you will need to scrape the site
* [Fixer](https://fixer.io/) - Well documented API in JSON format
* [Banxico](https://www.banxico.org.mx/SieAPIRest/service/v1/doc/consultaDatosSerieRango) - Service SF43718. API returns XML.

#
