package com.softim.moviesapi.utilities

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.softim.moviesapi.R

class ExceptionDialogFragment(val msj: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(msj)
            .setPositiveButton(getString(R.string.ok)) { _,_ ->

            }
            .create()

    companion object {
        const val TAG = "Exception Error"
    }
}