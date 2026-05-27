package com.example.modu.data.dataSource.remote.interceptor

import com.example.modu.data.dataSource.local.preference.AppPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

private const val HEADER_KEY = "Authorization"
class DeviceIdInterceptor @Inject constructor(

    private val appPreferences: AppPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        appPreferences.getDeviceId()?.let { deviceId ->
            requestBuilder.addHeader(HEADER_KEY, deviceId)
        }

        return chain.proceed(requestBuilder.build())
    }
}