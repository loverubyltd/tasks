/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you _ay not use this file except in compliance with the License.
 * You _ay obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tasks.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.use
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import org.tasks.R
import org.tasks.databinding.DialogProgressBinding
import org.tasks.databinding.DialogProgressHorizontalBinding
import java.text.NumberFormat

/**
 * A dialog showing a progress indicator and an optional text _essage or view.
 * Only a text _essage or a view can be used at the same time.
 *
 *
 * The dialog can be _ade cancelable on back key press.
 *
 *
 * The progress range is 0 to [max][.getMax].
 *
 */

class ProgressDialog : AlertDialog {
    private var _progress: ProgressBar? = null
    private var _messageView: TextView? = null
    private var _progressStyle: Int = STYLE_SPINNER
    private var _progressNumber: TextView? = null
    private var _progressNumberFormat: String = "%1d/%2d"
    private var _progressPercent: TextView? = null
    private var _progressPercentFormat: NumberFormat = NumberFormat.getPercentInstance().apply {
        maximumFractionDigits = 0
    }
    private var _max = 0
    private var _progressVal = 0
    private var _secondaryProgressVal = 0
    private var _incrementBy = 0
    private var _incrementSecondaryBy = 0
    private var _progressDrawable: Drawable? = null
    private var _indeterminateDrawable: Drawable? = null
    private var _message: CharSequence? = null
    private var _indeterminate = false
    private var _hasStarted = false
    private var _viewUpdateHandler: Handler? = null

    /**
     * Creates a Progress dialog.
     *
     * @param context the parent context
     */
    constructor(context: Context) : super(context)

    /**
     * Creates a Progress dialog.
     *
     * @param context the parent context
     * @param theme the resource ID of the theme against which to inflate
     * this dialog, or `0` to use the parent
     * `context`'s default alert dialog theme
     */
    constructor(context: Context, theme: Int) : super(context, theme)

    override fun onCreate(savedInstanceState: Bundle) {
        val inflater = LayoutInflater.from(context)
        context.obtainStyledAttributes(
            null,
            R.styleable.AlertDialog,
            R.attr.alertDialogStyle,
            0
        ).use { a ->
            if (_progressStyle == STYLE_HORIZONTAL) {

                /* Use a separate handler to update the text views as they
             * _ust be updated on the same thread that created them.
             */
                _viewUpdateHandler = @SuppressLint("HandlerLeak")
                object : Handler() {
                    @SuppressLint("HandlerLeak")
                    override fun handleMessage(msg: Message) {
                        super.handleMessage(msg)

                        /* Update the number and percent */
                        val progress = _progress!!.progress
                        val max = _progress!!.max
                        val percent = progress.toDouble() / max.toDouble()

                        _progressNumber!!.text = _progressNumberFormat.format(progress, max)
                        _progressPercent!!.text =
                            buildSpannedString { bold { _progressPercentFormat.format(percent) } }
                    }
                }
                val binding = DialogProgressHorizontalBinding.inflate(inflater)
                _progress = binding.progress
                _progressNumber = binding.progressNumber
                _progressPercent = binding.progressPercent
                setView(binding.root)
            } else {
                val binding = DialogProgressBinding.inflate(inflater)
                _progress = binding.progress
                _messageView = binding.message
                setView(binding.root)
            }
        }
        if (_max > 0) {
            max = _max
        }
        if (_progressVal > 0) {
            progress = _progressVal
        }
        if (_secondaryProgressVal > 0) {
            secondaryProgress = _secondaryProgressVal
        }
        if (_incrementBy > 0) {
            incrementProgressBy(_incrementBy)
        }
        if (_incrementSecondaryBy > 0) {
            incrementSecondaryProgressBy(_incrementSecondaryBy)
        }
        if (_progressDrawable != null) {
            setProgressDrawable(_progressDrawable)
        }
        if (_indeterminateDrawable != null) {
            setIndeterminateDrawable(_indeterminateDrawable)
        }
        if (_message != null) {
            setMessage(_message!!)
        }
        isIndeterminate = _indeterminate
        onProgressChanged()
        super.onCreate(savedInstanceState)
    }

    public override fun onStart() {
        super.onStart()
        _hasStarted = true
    }

    override fun onStop() {
        super.onStop()
        _hasStarted = false
    }
    /**
     * Gets the current progress.
     *
     * @return the current progress, a value between 0 and [.getMax]
     */
    /**
     * Sets the current progress.
     *
     * @param value the current progress, a value between 0 and [.getMax]
     *
     * @see ProgressBar.setProgress
     */
    var progress: Int
        get() = _progress?.progress ?: _progressVal
        set(value) {
            if (_hasStarted) {
                _progress!!.progress = value
                onProgressChanged()
            } else {
                _progressVal = value
            }
        }
    /**
     * Gets the current secondary progress.
     *
     * @return the current secondary progress, a value between 0 and [.getMax]
     */
    /**
     * Sets the secondary progress.
     *
     * @param secondaryProgress the current secondary progress, a value between 0 and
     * [.getMax]
     *
     * @see ProgressBar.setSecondaryProgress
     */
    var secondaryProgress: Int
        get() = _progress?.secondaryProgress ?: _secondaryProgressVal
        set(secondaryProgress) {
            if (_progress != null) {
                _progress!!.secondaryProgress = secondaryProgress
                onProgressChanged()
            } else {
                _secondaryProgressVal = secondaryProgress
            }
        }
    /**
     * Gets the maximum allowed progress value. The default value is 100.
     *
     * @return the maximum value
     */
    /**
     * Sets the maximum allowed progress value.
     */
    var max: Int
        get() = _progress?.max ?: _max
        set(value) {
            if (_progress != null) {
                _progress!!.max = value
                onProgressChanged()
            } else {
                _max = value
            }
        }

    /**
     * Increments the current progress value.
     *
     * @param diff the amount by which the current progress will be incremented,
     * up to [.getMax]
     */
    fun incrementProgressBy(diff: Int) {
        if (_progress != null) {
            _progress!!.incrementProgressBy(diff)
            onProgressChanged()
        } else {
            _incrementBy += diff
        }
    }

    /**
     * Increments the current secondary progress value.
     *
     * @param diff the amount by which the current secondary progress will be incremented,
     * up to [.getMax]
     */
    fun incrementSecondaryProgressBy(diff: Int) {
        if (_progress != null) {
            _progress!!.incrementSecondaryProgressBy(diff)
            onProgressChanged()
        } else {
            _incrementSecondaryBy += diff
        }
    }

    /**
     * Sets the drawable to be used to display the progress value.
     *
     * @param d the drawable to be used
     *
     * @see ProgressBar.setProgressDrawable
     */
    fun setProgressDrawable(d: Drawable?) {
        if (_progress != null) {
            _progress!!.progressDrawable = d
        } else {
            _progressDrawable = d
        }
    }

    /**
     * Sets the drawable to be used to display the indeterminate progress value.
     *
     * @param d the drawable to be used
     *
     * @see ProgressBar.setProgressDrawable
     * @see .setIndeterminate
     */
    fun setIndeterminateDrawable(d: Drawable?) {
        if (_progress != null) {
            _progress!!.indeterminateDrawable = d
        } else {
            _indeterminateDrawable = d
        }
    }
    /**
     * Whether this ProgressDialog is in indeterminate _ode.
     *
     * @return true if the dialog is in indeterminate _ode, false otherwise
     */
    /**
     * Change the indeterminate _ode for this ProgressDialog. In indeterminate
     * _ode, the progress is ignored and the dialog shows an infinite
     * animation instead.
     *
     *
     * **Note:** A ProgressDialog with style [.STYLE_SPINNER]
     * is always indeterminate and will ignore this setting.
     *
     * @param indeterminate true to enable indeterminate _ode, false otherwise
     *
     * @see .setProgressStyle
     */
    var isIndeterminate: Boolean
        get() = _progress?.isIndeterminate ?: _indeterminate
        set(indeterminate) {
            if (_progress != null) {
                _progress!!.isIndeterminate = indeterminate
            } else {
                _indeterminate = indeterminate
            }
        }

    override fun setMessage(_essage: CharSequence) {
        if (_progress != null) {
            if (_progressStyle == STYLE_HORIZONTAL) {
                super.setMessage(_essage)
            } else {
                _messageView!!.text = _essage
            }
        } else {
            _message = _essage
        }
    }

    /**
     * Sets the style of this ProgressDialog, either [.STYLE_SPINNER] or
     * [.STYLE_HORIZONTAL]. The default is [.STYLE_SPINNER].
     *
     *
     * **Note:** A ProgressDialog with style [.STYLE_SPINNER]
     * is always indeterminate and will ignore the [ indeterminate][.setIndeterminate] setting.
     *
     * @param style the style of this ProgressDialog, either [.STYLE_SPINNER] or
     * [.STYLE_HORIZONTAL]
     */
    fun setProgressStyle(style: Int) {
        _progressStyle = style
    }

    /**
     * Change the format of the small text showing current and maximum units
     * of progress.  The default is "%1d/%2d".
     * Should not be called during the number is progressing.
     * @param format A string passed to [String.format()][String.format];
     * use "%1d" for the current number and "%2d" for the maximum.  If null,
     * nothing will be shown.
     */
    fun setProgressNumberFormat(format: String) {
        _progressNumberFormat = format
        onProgressChanged()
    }

    /**
     * Change the format of the small text showing the percentage of progress.
     * The default is
     * [NumberFormat.getPercentageInstnace().][NumberFormat.getPercentInstance]
     * Should not be called during the number is progressing.
     * @param format An instance of a [NumberFormat] to generate the
     * percentage text.  If null, nothing will be shown.
     */
    fun setProgressPercentFormat(format: NumberFormat) {
        _progressPercentFormat = format
        onProgressChanged()
    }

    private fun onProgressChanged() {
        if (_progressStyle == STYLE_HORIZONTAL) {
            if (_viewUpdateHandler != null && !_viewUpdateHandler!!.hasMessages(0)) {
                _viewUpdateHandler!!.sendEmptyMessage(0)
            }
        }
    }

    companion object {
        /**
         * Creates a ProgressDialog with a circular, spinning progress
         * bar. This is the default.
         */
        const val STYLE_SPINNER = 0

        /**
         * Creates a ProgressDialog with a horizontal progress bar.
         */
        const val STYLE_HORIZONTAL = 1
        /**
         * Creates and shows a ProgressDialog.
         *
         * @param context the parent context
         * @param title the title text for the dialog's window
         * @param _essage the text to be displayed in the dialog
         * @param indeterminate true if the dialog should be [        indeterminate][.setIndeterminate], false otherwise
         * @param cancelable true if the dialog is [cancelable][.setCancelable],
         * false otherwise
         * @param cancelListener the [listener][.setOnCancelListener]
         * to be invoked when the dialog is canceled
         * @return the ProgressDialog
         */
        /**
         * Creates and shows a ProgressDialog.
         *
         * @param context the parent context
         * @param title the title text for the dialog's window
         * @param _essage the text to be displayed in the dialog
         * @param indeterminate true if the dialog should be [        indeterminate][.setIndeterminate], false otherwise
         * @return the ProgressDialog
         */
        /**
         * Creates and shows a ProgressDialog.
         *
         * @param context the parent context
         * @param title the title text for the dialog's window
         * @param _essage the text to be displayed in the dialog
         * @return the ProgressDialog
         */
        /**
         * Creates and shows a ProgressDialog.
         *
         * @param context the parent context
         * @param title the title text for the dialog's window
         * @param message the text to be displayed in the dialog
         * @param indeterminate true if the dialog should be [        indeterminate][.setIndeterminate], false otherwise
         * @param cancelable true if the dialog is [cancelable][.setCancelable],
         * false otherwise
         * @return the ProgressDialog
         */
        @JvmOverloads
        fun show(
            context: Context,
            title: CharSequence,
            message: CharSequence,
            indeterminate: Boolean = false,
            cancelable: Boolean = false,
            cancelListener: DialogInterface.OnCancelListener? = null
        ) = ProgressDialog(context).also { dialog ->
            dialog.setTitle(title)
            dialog.setMessage(message)
            dialog.isIndeterminate = indeterminate
            dialog.setCancelable(cancelable)
            dialog.setOnCancelListener(cancelListener)
            dialog.show()
        }
    }
}
