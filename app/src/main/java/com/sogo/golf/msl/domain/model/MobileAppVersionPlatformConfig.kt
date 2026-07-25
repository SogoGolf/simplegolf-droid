package com.sogo.golf.msl.domain.model

import com.google.gson.annotations.SerializedName

/**
 * One feature row of the tiered "What's New" card, authored in the web
 * Release Manager. [icon] is an SF Symbol name (shared config with iOS);
 * Android maps it to a Material icon.
 */
data class MobileAppVersionFeature(
    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("detail")
    val detail: String? = null
)

data class MobileAppVersionPlatformConfig(
    @SerializedName("minimumRequiredVersion")
    val minimumRequiredVersion: String = "",

    @SerializedName("optionalUpdatePromptEnabled")
    val optionalUpdatePromptEnabled: Boolean = false,

    @SerializedName("forceUpdateEnabled")
    val forceUpdateEnabled: Boolean = false,

    @SerializedName("updateMessage")
    val updateMessage: String = "",

    // Tiered "What's New" content — optional server-side, and nullable here
    // because Gson leaves absent fields null regardless of Kotlin defaults.
    // Consumers use .orEmpty(); absent content falls back to the
    // message-only card, matching pre-tiered behaviour.
    @SerializedName("features")
    val features: List<MobileAppVersionFeature>? = null,

    @SerializedName("fixRows")
    val fixRows: List<String>? = null,

    @SerializedName("fixes")
    val fixes: List<String>? = null,

    @SerializedName("footnote")
    val footnote: String? = null
)
