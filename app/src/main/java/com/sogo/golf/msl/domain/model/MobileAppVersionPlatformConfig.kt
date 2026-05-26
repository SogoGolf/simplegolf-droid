package com.sogo.golf.msl.domain.model

import com.google.gson.annotations.SerializedName

data class MobileAppVersionPlatformConfig(
    @SerializedName("minimumRequiredVersion")
    val minimumRequiredVersion: String = "",

    @SerializedName("optionalUpdatePromptEnabled")
    val optionalUpdatePromptEnabled: Boolean = false,

    @SerializedName("forceUpdateEnabled")
    val forceUpdateEnabled: Boolean = false,

    @SerializedName("updateMessage")
    val updateMessage: String = ""
)
