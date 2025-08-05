package com.sogo.golf.msl.domain.usecase.sogo_golfer

import com.sogo.golf.msl.data.network.api.CreateGolferDto
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslGolfer
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import com.sogo.golf.msl.shared_components.utils.DeviceInfoCollector
import com.sogo.golf.msl.shared_components.utils.FirebaseTokenCollector
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

class CreateGolferUseCase @Inject constructor(
    private val sogoMongoRepository: SogoMongoRepository,
    private val deviceInfoCollector: DeviceInfoCollector,
    private val firebaseTokenCollector: FirebaseTokenCollector
) {
    suspend operator fun invoke(
        mslGolfer: MslGolfer,
        authSystemUid: String
    ): NetworkResult<SogoGolfer> {
        val deviceToken = firebaseTokenCollector.getFirebaseToken() ?: ""
        
        val dateOfBirth = try {
            LocalDate.parse(mslGolfer.dateOfBirth).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            mslGolfer.dateOfBirth
        }
        
        val createGolferDto = CreateGolferDto(
            authSystemUid = authSystemUid,
            country = mslGolfer.country,
            dateOfBirth = dateOfBirth,
            deviceManufacturer = deviceInfoCollector.getDeviceManufacturer(),
            deviceModel = deviceInfoCollector.getDeviceModel(),
            deviceOS = deviceInfoCollector.getDeviceOS(),
            deviceOSVersion = deviceInfoCollector.getDeviceOSVersion(),
            deviceToken = deviceToken,
            email = mslGolfer.email ?: "",
            firstName = mslGolfer.firstName,
            gender = mslGolfer.gender ?: "",
            golflinkNo = mslGolfer.golfLinkNo,
            lastName = mslGolfer.surname,
            mobileNo = mslGolfer.mobileNo ?: "",
            postCode = mslGolfer.postCode ?: "",
            sogoAppVersion = deviceInfoCollector.getAppVersion(),
            state = mslGolfer.state ?: ""
        )
        
        return sogoMongoRepository.createGolfer(createGolferDto)
    }
}
