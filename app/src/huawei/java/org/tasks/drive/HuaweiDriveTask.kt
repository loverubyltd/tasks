package org.tasks.drive

import com.huawei.cloud.base.util.Logger
import java.util.concurrent.Future

abstract class HuaweiDriveTask : Runnable {
    /**
     * Return future for custom get timeout
     */
    /**
     * Used to obtain task information when the scheduled task is executed.
     */
    var future: Future<*>? = null

    override fun run() {
        try {
            call()
        } catch (e: Exception) {
            LOGGER.w("task error: $e")
        }
    }

    /**
     * DriveTask implementation
     */
    abstract fun call()

    /**
     * Used to terminate the current task
     */
    fun cancel(): Boolean {
        return if (null != future) {
            future!!.cancel(true)
        } else false
    }

    companion object {
        const val TAG = "DriveTask"
        val LOGGER = Logger.getLogger(TAG)
    }
}
