package org.tasks.drive

import android.content.Context
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.huawei.cloud.services.drive.DriveScopes
import com.huawei.hmf.tasks.Task
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject

class HuaweiClient @Inject constructor(
    @ApplicationContext context: Context
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

    fun signIn(activity: ComponentActivity) {
        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Call the account API to obtain account information.

        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleLoginResult(
                result
            )
        }.launch(service!!.signInIntent)
    }

    private fun handleLoginResult(result: ActivityResult?) {
    }

    private fun signInBackend(): Boolean {
        Timber.i("signInBackend")
        clearAccountInfo()

        if (service == null) {
            return false
        }

        val countDownLatch = CountDownLatch(1)
        val task: Task<AuthHuaweiId> = service!!.silentSignIn()
        task.addOnSuccessListener { authHuaweiId ->
            Timber.i("silentSignIn success")
            dealSignInResult(authHuaweiId)
            countDownLatch.countDown()
        }

        task.addOnFailureListener {
            Timber.i("silentSignIn error")
            countDownLatch.countDown()
        }

        try {
            countDownLatch.await(15, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Timber.i(
                "signInBackend catch InterruptedException"
            )
            countDownLatch.countDown()
        }

        return !TextUtils.isEmpty(accessToken)
    }

    private fun dealSignInResult(authHuaweiId: AuthHuaweiId?) {
// TODO()
    }


    /**
     * Save account info
     */
    private fun saveAccountInfo(signInHuaweiId: AuthHuaweiId?) {
        if (signInHuaweiId == null) {
            return
        }
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