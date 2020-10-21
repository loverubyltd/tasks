package org.tasks.drive


import android.content.Context
import android.net.Uri
import com.huawei.cloud.base.auth.DriveCredential
import com.huawei.cloud.base.http.HttpResponseException
import com.huawei.cloud.base.http.InputStreamContent
import com.huawei.cloud.base.json.GenericJson
import com.huawei.cloud.services.drive.Drive
import com.huawei.cloud.services.drive.DriveRequest
import com.huawei.cloud.services.drive.model.File
import com.todoroo.astrid.backup.BackupConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tasks.BuildConfig
import org.tasks.backup.HuaweiBackupConstants
import org.tasks.files.FileHelper
import org.tasks.preferences.Preferences
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject

class HuaweiDriveInvoker @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferences: Preferences,
    private val credentialManager: HuaweiDriveCredentialManager
) {
    private val credential: DriveCredential? = credentialManager.credential
    private val service = Drive.Builder(credential).apply {
        applicationName = String.format("Tasks/%s", BuildConfig.VERSION_NAME)
    }.build()


    @Throws(IOException::class)
    suspend fun getFile(folderId: String?): File? {
        return execute(service.files().get(folderId).setFields("id, trashed"))
    }

    @Throws(IOException::class)
    suspend fun delete(file: File) {
        execute(service.files().delete(file.id))
    }

    @Throws(IOException::class)
    suspend fun getFilesByPrefix(folderId: String?, vararg prefix: String?): List<File> {
        val namePredicate = prefix.joinToString(" or ") { "name contains '$it'" }
        val query = String.format(
            "'%s' in parents and ($namePredicate) and recycled = false and mimeType != '%s'",
            folderId, prefix, MIME_FOLDER
        )

        return execute(service.files()
            .list()
            .apply {
                queryParam = query
                // orderBy = "fileName"
                fields = "category,nextCursor,files(id,fileName,size,editedTime)"
                containers = "applicationData"
            })
            ?.files
            ?.filter { BackupConstants.isBackupFile(it.fileName) }
            ?.sortedWith(DRIVE_FILE_COMPARATOR)
            ?: emptyList()
    }


    @Throws(IOException::class)
    suspend fun createFolder(name: String?): File? {

        val appProperties = mapOf("appProperties" to "property")
        val folder = File().apply {
            fileName = name
            appSettings = appProperties
            mimeType = MIME_FOLDER
        }
        return execute(service.files().create(folder).setFields("id"))
    }

    @Throws(IOException::class)
    suspend fun createFile(folderId: String, uri: Uri?): File? {
        val mime = FileHelper.getMimeType(context, uri!!)
        val metadata = File().apply {
            parentFolder = listOf(folderId)
            mimeType = mime
            fileName = FileHelper.getFilename(context, uri)
        }
        val content = InputStreamContent(mime, context.contentResolver.openInputStream(uri))
        return execute(service.files().create(metadata, content))
    }

    @Synchronized
    @Throws(IOException::class)
    private suspend fun <T> execute(request: DriveRequest<T>): T? = execute(request, false)

    @Synchronized
    @Throws(IOException::class)
    private suspend fun <T> execute(
        request: DriveRequest<T>,
        retry: Boolean
    ): T? = withContext(Dispatchers.IO) {

        credentialManager.credential
        // credentialsAdapter.checkToken(account, DriveScopes.DRIVE_FILE)

        Timber.d("%s request: %s", caller, request)
        val response: T?
        response = try {
            request.execute()
        } catch (e: HttpResponseException) {
            return@withContext if (e.statusCode == 401 && !retry) {
                credentialManager.invalidateToken()
                execute(request, true)
            } else {
                throw e
            }
        }
        Timber.d("%s response: %s", caller, prettyPrint(response))
        return@withContext response
    }

    @Throws(IOException::class)
    private fun <T> prettyPrint(obj: T?): Any? {
        if (BuildConfig.DEBUG) {
            if (obj is GenericJson) {
                return (obj as GenericJson).toPrettyString()
            }
        }
        return obj
    }

    private val caller: String
        get() {
            if (BuildConfig.DEBUG) {
                try {
                    return Thread.currentThread().stackTrace[4].methodName
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
            return ""
        }


    companion object {
        private const val MIME_FOLDER = "application/vnd.huawei-apps.folder"
        private val DRIVE_FILE_COMPARATOR: Comparator<File> = compareBy {
            HuaweiBackupConstants.getTimestamp(it)
        }
    }
}