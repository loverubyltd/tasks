package org.tasks.drive

import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Manage runnable tasks
 */
class TaskManager {
    private val cached = Executors.newCachedThreadPool()

    /**
     * Execute runnable task
     */
    fun execute(driveTask: HuaweiDriveTask) {
        val future: Future<*> = cached.submit(driveTask)
        driveTask.future = future
    }

    companion object {
        /**
         * Get TaskManager instance
         */
        val instance = TaskManager()
    }
}
