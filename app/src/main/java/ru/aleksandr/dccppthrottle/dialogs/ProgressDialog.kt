/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import ru.aleksandr.dccppthrottle.R

class ProgressDialog(context: Context) : AlertDialog(context) {
    private val progressView: ProgressBar
    private val messageView: TextView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
        with(view) {
            progressView = findViewById<ProgressBar>(R.id.progressBar).apply {
                progress = 0
            }
            messageView = findViewById<TextView>(R.id.textMessage).apply {
                text = ""
                visibility = View.GONE
            }
        }
        setView(view)
        setCancelable(false)
    }

    fun setMax(value: Int) {
        with(progressView) {
            if (value > 0) {
                max = value
                isIndeterminate = false
            }
            else isIndeterminate = true
        }
    }

    fun incrementProgressBy(value: Int) {
        progressView.incrementProgressBy(value)
    }

    fun incrementProgress() {
        incrementProgressBy(1)
    }

    fun setProgress(value: Int) {
        progressView.progress = value
    }

    override fun setMessage(message: CharSequence?) {
        with(messageView) {
            text = message
            visibility =
                if(message.isNullOrEmpty()) View.GONE
                else View.VISIBLE
        }
    }

    fun setMessage(resId: Int) {
        setMessage(context.getString(resId))
    }

    fun setNegativeButton(
        text: CharSequence?,
        listener: DialogInterface.OnClickListener?
    ) {
        val whichButton = BUTTON_NEGATIVE
        super.setButton(whichButton, text, listener)
    }

    fun setNegativeButton(
        resId: Int,
        listener: DialogInterface.OnClickListener?
    ) {
        setNegativeButton(context.getString(resId), listener)
    }
}