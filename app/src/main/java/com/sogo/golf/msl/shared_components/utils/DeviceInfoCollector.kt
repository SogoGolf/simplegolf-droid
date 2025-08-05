package com.sogo.golf.msl.shared_components.utils

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoCollector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun getDeviceManufacturer(): String = Build.MANUFACTURER
    
    fun getDeviceModel(): String = Build.MODEL
    
    fun getDeviceOS(): String = "Android"
    
    fun getDeviceOSVersion(): String = Build.VERSION.RELEASE
    
    fun getAppVersion(): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
        return "${versionName} #${versionCode}"
    }
}
