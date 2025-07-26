package com.sogo.golf.msl.domain.model.msl.request_response

data class PostAuthTokenRequest(
    val code: String
)
/*
example payload (note the CAPS case):
{
    "Code": "voEgTbFt2KZukVOQaszzwZLyuRdbgnEgWrRGaKHx"
}
 */