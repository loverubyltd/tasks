package org.tasks.drive

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf
import com.huawei.cloud.base.auth.DriveCredential
import com.huawei.cloud.services.drive.DriveScopes
import com.huawei.cloud.services.drive.model.About
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.tasks.Callback
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject

class HuaweiClient @Inject constructor(
    @ApplicationContext val context: Context,
    private val drive: HuaweiDriveInvoker,
    private val credentialManager: HuaweiDriveCredentialManager

) {

    // Get AT Lock
    private val getATLock = ReentrantLock()


    private var authParams: HuaweiIdAuthParams =
        HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setAccessToken()
            .setIdToken()
            .setScopeList(scopeList)
            .createParams()

    private var service: HuaweiIdAuthService? = HuaweiIdAuthManager.getService(context, authParams)

    private var _accessToken: String? = null
    var accessToken: String?
        get() = _accessToken
        set(value) {
            _accessToken = value
        }
    private var _unionId: String? = null
    var unionId: String?
        get() = _unionId
        set(value) {
            _unionId = value
        }
    private var _deviceId: String? = null
    var deviceId: String?
        get() = _deviceId
        set(value) {
            _deviceId = value
        }
    private var _displayName: String? = null
    var displayName: String?
        get() = _displayName
        set(value) {
            _displayName = value
        }
    private var _status = 0
    var status: Int
        get() = _status
        set(value) {
            _status = value
        }
    private var _gender = 0
    var gender: Int
        get() = _gender
        set(value) {
            _gender = value
        }
    private var _grantedScopes: Set<*>? = null
    var grantedScopes: Set<*>?
        get() = _grantedScopes
        set(value) {
            _grantedScopes = value
        }
    private var _serviceCountryCode: String? = null
    var serviceCountryCode: String?
        get() = _serviceCountryCode
        set(value) {
            _serviceCountryCode = value
        }


    init {

    }

    private val refreshAT = DriveCredential.AccessMethod {


        return@AccessMethod refreshAccessToken()


    }

    fun signIn(
        activity: ComponentActivity,
        onSuccessListener: Callback<Int>,
        onFailureListener: Callback<ApiException>
    ) {
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            GlobalScope.launch {
                handleSignInResult(
                    result, onSuccessListener, onFailureListener
                )
            }
        }
            .launch(service!!.signInIntent)
    }

    private suspend fun handleSignInResult(
        result: ActivityResult?,
        onSuccessListener: Callback<Int>,
        onFailureListener: Callback<ApiException>
    ) {
        val data = result?.data
        val authHuaweiIdTask: Task<AuthHuaweiId> = HuaweiIdAuthManager.parseAuthResultFromIntent(
            data
        )
        if (authHuaweiIdTask.isSuccessful) {
            val huaweiAccount = authHuaweiIdTask.result
            val returnCode: Int = credentialManager.init(
                huaweiAccount.unionId,
                huaweiAccount.accessToken,
                refreshAT
            )

            val about = drive.about()
            if (about != null) {
                checkUpdateProtocol(about)
            }

            onSuccessListener.call(returnCode)
        } else {
            val exception = authHuaweiIdTask.exception
            if (exception is ApiException) {
                Timber.d("lgnIn failed: %d", exception.statusCode)
                onFailureListener.call(exception)
            }
        }
    }

    /**
     * Synchronously acquire access token, must be called in non-main thread
     *
     * @return accessToken or `null`
     */
    fun refreshAccessToken(): String? {
        Timber.i("refreshAccessToken begin")
        try {
            if (service != null) {
                getATLock.lock()
                try {
                    getAT()
                } finally {
                    getATLock.unlock()
                }
                Timber.d("refreshAccessToken return new")
            } else {
                Timber.e("refreshAccessToken client is null, return null")
            }
        } catch (e: Exception) {
            Timber.e(e, "refreshAccessToken exception, return null")
        }
        Timber.i("refreshAccessToken end")
        return accessToken
    }

    /**
     * Get accessToken
     */
    private fun getAT() {
        for (retry in 0..1) {
            Timber.i("signInBackend times: $retry")
            if (signInBackend()) {
                break
            }
        }
    }

    private fun signInBackend(): Boolean {
        Timber.i("signInBackend")
        clearAccountInfo()

        if (service == null) {
            return false
        }

        val countDownLatch = CountDownLatch(1)
        service!!.silentSignIn()
            .addOnSuccessListener { authHuaweiId ->
                Timber.i("silentSignIn success")
                handleSignInBackendResult(authHuaweiId)
                countDownLatch.countDown()
            }
            .addOnFailureListener {
                Timber.i("silentSignIn error")
                countDownLatch.countDown()
            }

        try {
            countDownLatch.await(15, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Timber.i("signInBackend catch InterruptedException")
            countDownLatch.countDown()
        }

        return !accessToken.isNullOrBlank()
    }

    private fun handleSignInBackendResult(authHuaweiId: AuthHuaweiId) {
        if (authHuaweiId.accessToken.isBlank()) {
            Timber.e("  get accessToken is null.")
            return
        }

        Timber.i(" handleSignInBackendResult get new AT successfully")
        saveAccountInfo(authHuaweiId)
    }


    /**
     * Check whether the user needs to be prompted to update the protocol.
     *
     * @param about About response that is returned.
     */
    private fun checkUpdateProtocol(about: About) {
        Timber.d("checkUpdate: %s", about.toString())
        val updateValue = about["needUpdate"]
        var isNeedUpdate = false
        if (updateValue is Boolean) {
            isNeedUpdate = updateValue
        }
        if (!isNeedUpdate) {
            return
        }
        val urlValue = about["updateUrl"]
        var url = ""
        if (urlValue is String) {
            url = urlValue
        }
        if (TextUtils.isEmpty(url)) {
            return
        }
        val uri = Uri.parse(url)
        if ("https" != uri.scheme) {
            return
        }
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(context, intent, bundleOf())
        } catch (e: ActivityNotFoundException) {
            Timber.e("Activity Not found")
        }
    }

    /**
     * Save account info
     */
    private fun saveAccountInfo(signInHuaweiId: AuthHuaweiId) {

        unionId = signInHuaweiId.unionId
        deviceId = signInHuaweiId.openId
        displayName = signInHuaweiId.displayName
        status = signInHuaweiId.status
        gender = signInHuaweiId.gender
        grantedScopes = signInHuaweiId.authorizedScopes
        serviceCountryCode = signInHuaweiId.serviceCountryCode
        accessToken = signInHuaweiId.accessToken
    }

    /**
     * Clear account info
     */
    private fun clearAccountInfo() {
        unionId = null
        deviceId = null
        displayName = null
        status = 0
        gender = 0
        grantedScopes = null
        serviceCountryCode = null
        accessToken = null
    }


    companion object {

        private val scopeList: List<Scope> = listOf(
            Scope(DriveScopes.SCOPE_DRIVE), // All permissions, except permissions for the app data folder.
            Scope(DriveScopes.SCOPE_DRIVE_READONLY),  // Permissions to view file metadata and content.
            Scope(DriveScopes.SCOPE_DRIVE_FILE),  // Permissions to view and manage files.
            Scope(DriveScopes.SCOPE_DRIVE_METADATA), // Permissions to view and manage file metadata, excluding file content.
            Scope(DriveScopes.SCOPE_DRIVE_METADATA_READONLY),  // Permissions to view file metadata, excluding file content.
            Scope(DriveScopes.SCOPE_DRIVE_APPDATA), // Permissions to upload and store app data.
            HuaweiIdAuthAPIManager.HUAWEIID_BASE_SCOPE // Basic account permissions._
        )
    }
}