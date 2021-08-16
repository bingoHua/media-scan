package com.wt.cloudmedia.common.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.wt.cloudmedia.R

class LoadingDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.dialog_loading).apply {
            this.setContentView(
                LayoutInflater.from(requireContext()).inflate(R.layout.loading_dialog_layout, null)
            )
        }
    }
}