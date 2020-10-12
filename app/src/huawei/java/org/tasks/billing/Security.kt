/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tasks.billing

import android.text.TextUtils
import android.util.Base64
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

object Security {

    private const val TAG = "HMS_LOG_CipherUtil"
    private const val SIGN_ALGORITHMS = "SHA256WithRSA"

    /**
     * To check the signature for the data returned from the interface.
     *
     * @param content Unsigned data.
     * @param sign the signature for content.
     * @param publicKey the public key of the application.
     *
     * @return boolean
     */
    fun doCheck(content: String, sign: String?, publicKey: String?): Boolean {
        if (TextUtils.isEmpty(publicKey)) {
            Timber.e( "publicKey is null")
            return false
        }
        try {
            val keyFactory = KeyFactory.getInstance("RSA")
            val encodedKey = Base64.decode(publicKey, Base64.DEFAULT)
            val pubKey = keyFactory.generatePublic(X509EncodedKeySpec(encodedKey))
            val signature = Signature.getInstance(SIGN_ALGORITHMS)
            signature.initVerify(pubKey)
            signature.update(content.toByteArray(charset("utf-8")))
            return signature.verify(Base64.decode(sign, Base64.DEFAULT))
        } catch (ex: NoSuchAlgorithmException) {
            Timber.e(ex, "doCheck NoSuchAlgorithmException ")
        } catch (ex: InvalidKeySpecException) {
            Timber.e(ex, "doCheck InvalidKeySpecException ")
        } catch (ex: InvalidKeyException) {
            Timber.e(ex, "doCheck InvalidKeyException ")
        } catch (ex: SignatureException) {
            Timber.e(ex, "doCheck SignatureException ")
        } catch (ex: UnsupportedEncodingException) {
            Timber.e(ex, "doCheck UnsupportedEncodingException ")
        }
        return false
    }
}