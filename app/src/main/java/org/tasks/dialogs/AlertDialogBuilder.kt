package org.tasks.dialogs

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.ListAdapter
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.tasks.locale.Locale

class AlertDialogBuilder internal constructor(
    private val context: Context,
    private val locale: Locale
) {
    private val builder: AlertDialog.Builder

    fun setMessage(message: Int, vararg formatArgs: Any?): AlertDialogBuilder {
        return setMessage(context.getString(message, *formatArgs))
    }

    fun setMessage(message: String?): AlertDialogBuilder = apply {
        builder.setMessage(message)
    }

    fun setPositiveButton(
        ok: Int, onClickListener: DialogInterface.OnClickListener?
    ): AlertDialogBuilder = apply {
        builder.setPositiveButton(ok, onClickListener)
    }

    fun setNegativeButton(
        cancel: Int, onClickListener: DialogInterface.OnClickListener?
    ): AlertDialogBuilder = apply {
        builder.setNegativeButton(cancel, onClickListener)
    }

    fun setTitle(title: Int): AlertDialogBuilder = apply {
        builder.setTitle(title)
    }

    fun setTitle(title: Int, vararg formatArgs: Any?): AlertDialogBuilder = apply {
        builder.setTitle(context.getString(title, *formatArgs))
    }

    fun setItems(
        strings: List<String>, onClickListener: DialogInterface.OnClickListener?
    ): AlertDialogBuilder = setItems(strings.toTypedArray(), onClickListener)

    fun setItems(
        strings: Array<String>, onClickListener: DialogInterface.OnClickListener?
    ): AlertDialogBuilder = apply {
        builder.setItems(addDirectionality(strings.clone()), onClickListener)
    }

    fun setView(dialogView: View?): AlertDialogBuilder = apply {
        builder.setView(dialogView)
    }

    fun setOnCancelListener(onCancelListener: DialogInterface.OnCancelListener?): AlertDialogBuilder =
        apply {
            builder.setOnCancelListener(onCancelListener)
        }

    fun setSingleChoiceItems(
        strings: List<String>, selectedIndex: Int, onClickListener: DialogInterface.OnClickListener?
    ): AlertDialogBuilder =
        setSingleChoiceItems(strings.toTypedArray(), selectedIndex, onClickListener)

    fun setSingleChoiceItems(
        strings: Array<String>,
        selectedIndex: Int,
        onClickListener: DialogInterface.OnClickListener?
    ): AlertDialogBuilder = apply {
        builder.setSingleChoiceItems(addDirectionality(strings), selectedIndex, onClickListener)
    }

    private fun addDirectionality(strings: Array<String>): Array<String> {
        for (i in strings.indices) {
            strings[i] = withDirectionality(strings[i])
        }
        return strings
    }

    private fun withDirectionality(string: String): String =
        locale.directionalityMark.toString() + string

    fun setSingleChoiceItems(
        adapter: ListAdapter?, selectedIndex: Int, onClickListener: DialogInterface.OnClickListener?
    ): AlertDialogBuilder = apply {
        builder.setSingleChoiceItems(adapter, selectedIndex, onClickListener)
    }

    fun setNeutralButton(
        resId: Int, onClickListener: DialogInterface.OnClickListener?
    ): AlertDialogBuilder = apply {
        builder.setNeutralButton(resId, onClickListener)
    }

    fun setTitle(title: String?): AlertDialogBuilder = apply {
        builder.setTitle(title)
    }

    fun setOnDismissListener(
        onDismissListener: DialogInterface.OnDismissListener?
    ): AlertDialogBuilder = apply {
        builder.setOnDismissListener(onDismissListener)
    }

    fun create(): AlertDialog = builder.create()

    fun show(): AlertDialog = create().also { dialog ->
        dialog.show()
        locale.applyDirectionality(dialog)
    }

    init {
        builder = MaterialAlertDialogBuilder(context)
    }
}
