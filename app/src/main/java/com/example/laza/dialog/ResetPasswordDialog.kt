package com.example.laza.dialog

import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.laza.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth

fun Fragment.setUpBottomSheetDialog(
    OnSendClick: (String) -> Unit
) {
    val dialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
    val view = layoutInflater.inflate(R.layout.reset_password_dialog, null)
    dialog.setContentView(view)
    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    dialog.show()

    val buttonsSend = view.findViewById<Button>(R.id.buttonSendResetPassword)
    val buttonsCancel = view.findViewById<Button>(R.id.buttonCancelResetPassword)

    buttonsSend.setOnClickListener {
        val email  = FirebaseAuth.getInstance().currentUser?.email.toString()
        OnSendClick(email)
        dialog.dismiss()
    }

    buttonsCancel.setOnClickListener {
        dialog.dismiss()
    }
}