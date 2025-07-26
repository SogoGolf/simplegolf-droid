package com.sogo.golf.msl.domain.model.msl.request_response

data class PutMarker(
    val playerGolfLinkNumber: String
)

data class PutMarkerResponse(
    val errorMessage: String? = null
)


