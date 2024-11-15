package edu.vt.cs5254.bucketlist

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import edu.vt.cs5254.bucketlist.databinding.FragmentProgressDialogBinding

class ProgressDialogFragment : DialogFragment() {

    companion object {
        const val REQUEST_KEY = "ProgressDialogRequestKey"
        const val BUNDLE_KEY = "ProgressDialogResult"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentProgressDialogBinding.inflate(layoutInflater)

        // Create the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.progress_dialog_title) // "Add Progress"
            .setPositiveButton(R.string.progress_dialog_positive, null) // "Add"
            .setNegativeButton(R.string.progress_dialog_negative, null) // "Cancel"
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            // Ensure the positive button is initially disabled
            positiveButton.isEnabled = false
            // Define click listener for the positive button
            positiveButton.setOnClickListener {
                val resultText = binding.progressText.text.toString()
                setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(BUNDLE_KEY to resultText)
                )
                dialog.dismiss()
            }
        }

        // Set up text change listener to enable/disable the positive button based on input
        binding.progressText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                if (positiveButton != null) {
                    positiveButton.isEnabled = s?.isNotBlank() == true
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val positiveButton = (dialog as? AlertDialog)?.getButton(AlertDialog.BUTTON_POSITIVE)
        outState.putBoolean("isPositiveButtonEnabled", positiveButton?.isEnabled == true)
    }
}
