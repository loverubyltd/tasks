package org.tasks.dialogs

import android.app.Activity
import org.tasks.locale.Locale
import javax.inject.Inject

class DialogBuilder @Inject constructor(
    private val activity: Activity,
    private val locale: Locale
) {
    fun newDialog(): AlertDialogBuilder {
        return AlertDialogBuilder(activity, locale)
    }

    fun newDialog(title: Int): AlertDialogBuilder {
        return newDialog().setTitle(title)
    }

    fun newDialog(title: String?): AlertDialogBuilder {
        return newDialog().setTitle(title)
    }

    fun newDialog(title: Int, vararg formatArgs: Any?): AlertDialogBuilder {
        return newDialog().setTitle(title, *formatArgs)
    }

    fun newProgressDialog(): ProgressDialog {
        return ProgressDialog(activity)
    }

    fun newProgressDialog(messageId: Int): ProgressDialog = newProgressDialog().apply {
        isIndeterminate = true
        progressStyle = ProgressDialog.STYLE_SPINNER
        setMessage(activity.getString(messageId))
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}
