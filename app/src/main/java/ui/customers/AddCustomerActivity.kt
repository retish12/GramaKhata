package com.example.finalprojectintern

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddCustomerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_customer)

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etNotes = findViewById<EditText>(R.id.etNotes)
        val tvAvatar = findViewById<TextView>(R.id.tvAvatarPreview)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<TextView>(R.id.btnBack)

        // Back button
        btnBack.setOnClickListener { finish() }

        // Live avatar preview — updates as user types name
        etName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                tvAvatar.text = if (s.isNullOrEmpty()) "?"
                else s.first().uppercase()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Save button
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val notes = etNotes.text.toString().trim()

            // Validation
            when {
                name.isEmpty() -> {
                    etName.error = "Please enter customer name"
                    etName.requestFocus()
                }
                phone.isEmpty() -> {
                    etPhone.error = "Please enter phone number"
                    etPhone.requestFocus()
                }
                phone.length < 10 -> {
                    etPhone.error = "Enter valid 10-digit number"
                    etPhone.requestFocus()
                }
                else -> {
                    // Pass new customer back to MainActivity
                    val result = Intent()
                    result.putExtra("name", name)
                    result.putExtra("phone", "+91 ${phone.chunked(5).joinToString(" ")}")
                    result.putExtra("notes", notes)
                    setResult(Activity.RESULT_OK, result)
                    Toast.makeText(this, "✅ $name added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}