package org.tasks.backup

 import com.huawei.cloud.services.drive.model.File
 import com.todoroo.astrid.backup.BackupConstants

object HuaweiBackupConstants {
    fun getTimestamp(file: File): Long? =
        BackupConstants.getTimestampFromFilename(file.fileName) ?: file.editedTime?.value
}