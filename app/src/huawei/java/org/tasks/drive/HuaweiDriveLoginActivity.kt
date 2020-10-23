package org.tasks.drive

import android.Manifest
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.huawei.cloud.base.auth.DriveCredential.AccessMethod
import com.huawei.cloud.client.exception.DriveCode
import com.todoroo.andlib.utility.AndroidUtilities
import dagger.hilt.android.AndroidEntryPoint
import org.tasks.dialogs.DialogBuilder
import org.tasks.injection.InjectingAppCompatActivity
import org.tasks.preferences.Preferences
import org.tasks.ui.Toaster
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AndroidUtilities.atLeastQ()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1)
        }

        performSignIn()
    }

    private fun performSignIn() {
        huaweiClient.signIn(this,
            { returnCode ->

                when (returnCode) {
                    DriveCode.SUCCESS -> toaster.longToast("login ok")
                    DriveCode.SERVICE_URL_NOT_ENABLED -> toaster.longToast("drive is not enabled")
                    else -> toaster.longToast("login error")
                }
            },
            {
                toaster.longToast("Login to Huawei Drive failed")
            })
    }

    companion object {
        private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

    }
}