package com.credijusto.challenge.usdtomxn.ratesources.fixer.vo.enum

/**
 * Enums with comments are custom accordingly to the ones that uses Fixer.io
 * All other enums are taken from gist.github.com/vlio20/StatusCode.kt
 */
enum class StatusCode(val code: Int) {
    Continue(100),
    InvalidAPIKey(101),             //No API Key was specified or an invalid API Key was specified.
    AccountInactive(102),           //The account this API request is coming from is inactive.
    EndpointNotFound(103),          //The requested API endpoint does not exist.
    QuotaReached(104),              //The maximum allowed API amount of monthly API requests has been reached.
    FeatureNotIncluded(105),        //The current subscription plan does not support this API endpoint.
    NoResults(106),                 //The current request did not return any results.

    OK(200),
    InvalidBaseCurrency(201),           //An invalid base currency has been entered.
    InvalidSymbols(202),                //One or more invalid symbols have been specified.
    NonAuthoritativeInformation(203),
    NoContent(204),
    ResetContent(205),
    PartialContent(206),
    MultiStatus(207),
    AlreadyReported(208),
    IMUsed(226),

    MultipleChoices(300),
    NoDateSpecified(301),               //No date has been specified. [historical]
    InvalidDateSpecified(302),          //An invalid date has been specified. [historical, convert]
    SeeOther(303),
    NotModified(304),
    UseProxy(305),
    TemporaryRedirect(307),
    PermanentRedirect(308),

    BadRequest(400),
    Unauthorized(401),
    PaymentRequired(402),
    InvalidAmountSpecified(403),        //No or an invalid amount has been specified. [convert]
    NotFound(404),                      //The requested resource does not exist.
    MethodNotAllowed(405),
    NotAcceptable(406),
    ProxyAuthenticationRequired(407),
    RequestTimeout(408),
    Conflict(409),
    Gone(410),
    LengthRequired(411),
    PreconditionFailed(412),
    PayloadTooLarge(413),
    URITooLong(414),
    UnsupportedMediaType(415),
    RangeNotSatisfiable(416),
    ExpectationFailed(417),
    IAmATeapot(418),
    MisdirectedRequest(421),
    UnprocessableEntity(422),
    Locked(423),
    FailedDependency(424),
    UpgradeRequired(426),
    PreconditionRequired(428),
    TooManyRequests(429),
    RequestHeaderFieldsTooLarge(431),
    UnavailableForLegalReasons(451),

    InternalServerError(500),
    InvalidTimeFrameSpecified(501),         //No or an invalid timeframe has been specified. [timeseries]
    InvalidStartDaySpecified(502),          //No or an invalid "start_date" has been specified. [timeseries, fluctuation]
    InvalidEndDateSpecified(503),           //No or an invalid "end_date" has been specified. [timeseries, fluctuation]
    InvalidTimeFrameFluctuation(504),       //An invalid timeframe has been specified. [timeseries, fluctuation]
    TimeFrameSpecifiedTooLong(505),         //The specified timeframe is too long, exceeding 365 days. [timeseries, fluctuation]

    VariantAlsoNegotiates(506),
    InsufficientStorage(507),
    LoopDetected(508),
    NotExtended(510),
    NetworkAuthenticationRequired(511),

    Unknown(0);

    companion object {
        fun getFromValue(code: Int): StatusCode {
            for (statusCode in values()) {
                if (code == statusCode.code) {
                    return statusCode
                }
            }
            return Unknown
        }
    }
}

