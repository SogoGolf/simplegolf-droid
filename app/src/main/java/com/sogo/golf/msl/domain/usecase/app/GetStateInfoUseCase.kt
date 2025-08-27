package com.sogo.golf.msl.domain.usecase.app

import com.sogo.golf.msl.domain.model.StateInfo
import javax.inject.Inject

class GetStateInfoUseCase @Inject constructor() {
    
    operator fun invoke(stateCode: String?): StateInfo? {
        if (stateCode.isNullOrBlank()) return null
        
        return when (stateCode.lowercase()) {
            "nsw", "new south wales" -> StateInfo(
                alpha2 = "AU",
                name = "New South Wales",
                shortName = "NSW"
            )
            "qld", "queensland" -> StateInfo(
                alpha2 = "AU",
                name = "Queensland",
                shortName = "QLD"
            )
            "vic", "victoria" -> StateInfo(
                alpha2 = "AU",
                name = "Victoria",
                shortName = "VIC"
            )
            "wa", "western australia" -> StateInfo(
                alpha2 = "AU",
                name = "Western Australia",
                shortName = "WA"
            )
            "sa", "south australia" -> StateInfo(
                alpha2 = "AU",
                name = "South Australia",
                shortName = "SA"
            )
            "tas", "tasmania" -> StateInfo(
                alpha2 = "AU",
                name = "Tasmania",
                shortName = "TAS"
            )
            "nt", "northern territory" -> StateInfo(
                alpha2 = "AU",
                name = "Northern Territory",
                shortName = "NT"
            )
            "act", "australian capital territory" -> StateInfo(
                alpha2 = "AU",
                name = "Australian Capital Territory",
                shortName = "ACT"
            )
            else -> null
        }
    }
}
