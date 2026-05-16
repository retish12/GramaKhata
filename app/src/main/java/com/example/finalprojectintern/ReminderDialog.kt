package com.example.finalprojectintern

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch

object ReminderDialog {

    fun show(
        context: Context,
        customerName: String,
        customerPhone: String,
        balance: Int,
        lifecycleScope: LifecycleCoroutineScope
    ) {
        val loadingDialog = AlertDialog.Builder(context)
            .setTitle("Generating AI Reminder...")
            .setMessage("Please wait while AI creates a personalized message...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        lifecycleScope.launch {
            val message = OpenAIService.generateReminder(
                customerName = customerName,
                balance = balance,
                shopName = "our shop"
            )
            loadingDialog.dismiss()
            showPreviewDialog(context, customerName, customerPhone, message)
        }
    }

    private fun showPreviewDialog(
        context: Context,
        customerName: String,
        customerPhone: String,
        message: String
    ) {
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(40, 20, 40, 10)

        val label = TextView(context)
        label.text = "AI Generated Message (you can edit):"
        label.setPadding(0, 0, 0, 10)
        label.setTextColor(Color.parseColor("#BA7517"))
        container.addView(label)

        val editText = EditText(context)
        editText.setText(message)
        editText.setPadding(20, 20, 20, 20)
        editText.minLines = 5
        editText.setBackgroundColor(Color.parseColor("#FFF8E7"))
        container.addView(editText)

        AlertDialog.Builder(context)
            .setTitle("Send Reminder to $customerName")
            .setView(container)
            .setPositiveButton("Send on WhatsApp") { _, _ ->
                sendWhatsApp(context, customerPhone, editText.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Copy Message") { _, _ ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                        as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText("reminder", editText.text.toString())
                )
                Toast.makeText(context, "Message copied!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun sendWhatsApp(context: Context, phone: String, message: String) {
        val cleanPhone = phone.replace("[^0-9]".toRegex(), "")
        val uri = Uri.parse("https://wa.me/91$cleanPhone?text=${Uri.encode(message)}")
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (ex: Exception) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }
}