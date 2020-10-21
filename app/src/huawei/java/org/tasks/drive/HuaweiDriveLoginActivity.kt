package org.tasks.drive

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import com.huawei.cloud.base.auth.DriveCredential.AccessMethod
import com.huawei.cloud.client.exception.DriveCode
import com.huawei.cloud.services.drive.DriveScopes
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.account.request.AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import dagger.hilt.android.AndroidEntryPoint
import org.tasks.dialogs.DialogBuilder
import org.tasks.injection.InjectingAppCompatActivity
import org.tasks.preferences.Preferences
import org.tasks.ui.Toaster
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HuaweiDriveLoginActivity : InjectingAppCompatActivity() {

    @Inject
    lateinit var dialogBuilder: DialogBuilder

    @Inject
    lateinit var preferences: Preferences

    @Inject
    lateinit var credentialManager: HuaweiDriveCredentialManager

    @Inject
    lateinit var huaweiClient: HuaweiClient

    @Inject
    lateinit var toaster: Toaster


    private val refreshAT = AccessMethod {
        /**
         * Simplified code snippet for demonstration purposes. For the complete code snippet,
         * please go to Client Development > Obtaining Authentication Information > Store Authentication Information
         * in the HUAWEI Drive Kit Development Guide.
         **/
        return@AccessMethod credentialManager.credential.accessToken
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1)
        }
        performLogin()
    }

    private fun performLogin() {
        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)


    }

    private fun handleLoginResult(result: ActivityResult) {
        val data = result.data
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
            when (returnCode) {
                DriveCode.SUCCESS -> {
                    toaster.longToast("login ok")
                }
                DriveCode.SERVICE_URL_NOT_ENABLED -> {
                    toaster.longToast("drive is not enabled")
                }
                else -> {
                    toaster.longToast("login error")
                }
            }
        } else {
            Timber.d(
                "handleLoginResult, signIn failed: %d",
                (authHuaweiIdTask.exception as? ApiException)?.statusCode
            )
            toaster.longToast("Login to Huawei Driver failed")
        }

    }

    companion object {
        private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

    }
}