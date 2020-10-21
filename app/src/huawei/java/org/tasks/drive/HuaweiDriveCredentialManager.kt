/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tasks.drive

import android.content.Context
import android.text.TextUtils
import com.huawei.cloud.base.auth.DriveCredential
import com.huawei.cloud.client.exception.DriveCode
import com.huawei.hmf.tasks.Task
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject


/**
 * Credential management class.
 */
 class HuaweiDriveCredentialManager @Inject constructor(@ApplicationContext   context: Context)  {

    private val getATLock = ReentrantLock()


    /**
     * Obtain DriveCredential.
     *
     * @return DriveCredential
     */
    var credential: DriveCredential? = null
        private set

    /**
     * Initialize Drive based on the context and HUAWEI ID information including unionId, countrycode, and accessToken.
     * When the current accessToken expires, register an AccessMethod and obtain a new accessToken.
     *
     * @param unionID from HwID
     * @param accessToken  access token
     * @param refreshAT a callback to refresh AT
     * @return Int result code DriveCode
     */
    fun init(unionID: String, accessToken: String, refreshAT: DriveCredential.AccessMethod?): Int {
        return if (unionID.isBlank() || accessToken.isBlank()) {
            DriveCode.ERROR
        } else {
            credential = DriveCredential.Builder(unionID, refreshAT)
                .build()
                .also { it.accessToken = accessToken }
            DriveCode.SUCCESS
        }
    }

    fun invalidateToken() {
        credential = null
    }

    /**
     * Exit Drive and clear all cache information generated during use of Drive.
     */
    fun exit(context: Context) {
        // Delete cache files.
        deleteFile(context.cacheDir)
        deleteFile(context.filesDir)
    }


    companion object {
        /**
         * Delete cache files.
         *
         * @param file Designated cache file.
         */
        private fun deleteFile(file: java.io.File) {
            if (!file.exists()) {
                return
            }
            if (file.isDirectory) {
                file.listFiles()?.forEach { deleteFile(it) }
            }
        }
    }
}