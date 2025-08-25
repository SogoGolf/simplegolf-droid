package com.sogo.golf.msl.domain.usecase.app

import com.sogo.golf.msl.BuildConfig
import javax.inject.Inject

class GetMobileAppVersionUseCase @Inject constructor() {
    operator fun invoke(): String {
        return "${BuildConfig.VERSION_NAME} #${BuildConfig.VERSION_CODE}"
    }
}
