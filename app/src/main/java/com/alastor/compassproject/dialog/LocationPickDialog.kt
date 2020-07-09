package com.alastor.compassproject.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import com.alastor.compassproject.R
import java.util.regex.Pattern

class LocationPickDialog : DialogFragment() {

    companion object {
        @JvmField
        val TAG = LocationPickDialog::class.java.simpleName;
        private const val ARG_TITLE = "arg_title"
        private const val ARG_REQUEST_KEY = "requestKey"

        @JvmStatic
        fun create(title: String, requestKey: String): LocationPickDialog {
            return LocationPickDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_REQUEST_KEY, requestKey)
                }
            }
        }
    }

    private lateinit var listener: NotifyDialogListener
    private var requestKey: String = ""
    private lateinit var inputEt: EditText

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NotifyDialogListener) {
            listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AppCompatDialog(activity, R.style.DialogStyleNoTitle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_location_pick, container, false)
        var title = ""
        arguments?.let {
            requestKey = it.getString(ARG_REQUEST_KEY, "")
            title = it.getString(ARG_TITLE, "")
        }
        inputEt = view.findViewById(R.id.edit_input)
        view.findViewById<TextView>(R.id.text_title).text = title
        return view
    }

    override fun onStop() {
        super.onStop()
        val inputValue = inputEt.text.toString()
        val matcher = Pattern.compile("\\d+(?:\\.\\d+)?").matcher(inputValue)
        if (!TextUtils.isEmpty(inputValue) && matcher.find())
            listener.onDialogResponse(requestKey, inputValue.toDouble())
    }

    public interface NotifyDialogListener {
        public fun onDialogResponse(requestKey: String, locationValue: Double);
    }
}