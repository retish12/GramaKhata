package com.example.finalprojectintern

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CustomerDetailActivity : AppCompatActivity() {

    private val viewModel: CustomerViewModel by viewModels()
    private var customerId = 0
    private var netBalance = 0
    private var totalGiven = 0
    private var totalTaken = 0
    private lateinit var customerName: String
    private lateinit var customerPhone: String
    private lateinit var tvNetBalance: TextView
    private val transactions = mutableListOf<TransactionItem>()
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_detail)

        // Get data
        customerId = intent.getIntExtra("customerId", 0)
        customerName = intent.getStringExtra("name") ?: "Customer"
        customerPhone = intent.getStringExtra("phone") ?: ""
        netBalance = intent.getIntExtra("balance", 0)
        totalGiven = intent.getIntExtra("totalGiven", 0)
        totalTaken = intent.getIntExtra("totalTaken", 0)

        // Set UI
        findViewById<TextView>(R.id.tvCustomerName).text = customerName
        findViewById<TextView>(R.id.tvPhone).text = customerPhone
        tvNetBalance = findViewById(R.id.tvNetBalance)
        updateBalanceDisplay()

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        // RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTransactions)
        adapter = TransactionAdapter(transactions)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observe transactions from DB
        lifecycleScope.launch {
            viewModel.getTransactions(customerId).collect { dbTransactions ->
                transactions.clear()
                transactions.addAll(dbTransactions.map {
                    TransactionItem(it.note, it.date, it.amount, it.isGive)
                })
                adapter.notifyDataSetChanged()
            }
        }

        // Give Credit
        findViewById<Button>(R.id.btnGive).setOnClickListener {
            showTransactionDialog(true)
        }

        // Take Payment
        findViewById<Button>(R.id.btnTake).setOnClickListener {
            showTransactionDialog(false)
        }

        // WhatsApp Reminder
        findViewById<Button>(R.id.btnReminder).setOnClickListener {
            sendWhatsAppReminder()
        }
    }

    private fun showTransactionDialog(isGive: Boolean) {
        val title = if (isGive) "Give Credit" else "Take Payment"

        val container = android.widget.LinearLayout(this)
        container.orientation = android.widget.LinearLayout.VERTICAL
        container.setPadding(60, 20, 60, 0)

        val amountInput = EditText(this)
        amountInput.hint = "Amount in ₹"
        amountInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        container.addView(amountInput)

        val noteInput = EditText(this)
        noteInput.hint = "Note (optional)"
        container.addView(noteInput)

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(container)
            .setPositiveButton("Confirm") { _, _ ->
                val amount = amountInput.text.toString().toIntOrNull()
                if (amount != null && amount > 0) {
                    val note = noteInput.text.toString()
                        .ifEmpty { if (isGive) "Credit given" else "Payment received" }
                    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Date())

                    // Get current customer object
                    lifecycleScope.launch {
                        val customer = Customer(
                            id = customerId,
                            name = customerName,
                            phone = customerPhone,
                            totalGiven = totalGiven,
                            totalTaken = totalTaken
                        )
                        if (isGive) {
                            viewModel.giveCredit(customer, amount, note, date)
                            totalGiven += amount
                            netBalance += amount
                        } else {
                            viewModel.takePayment(customer, amount, note, date)
                            totalTaken += amount
                            netBalance -= amount
                        }
                        updateBalanceDisplay()
                        Toast.makeText(
                            this@CustomerDetailActivity,
                            if (isGive) "Credit ₹$amount added!" else "Payment ₹$amount recorded!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBalanceDisplay() {
        if (netBalance <= 0) {
            tvNetBalance.text = "Settled ✓"
            tvNetBalance.setTextColor(0xFF1D9E75.toInt())
        } else {
            tvNetBalance.text = "₹$netBalance due"
            tvNetBalance.setTextColor(0xFFE24B4A.toInt())
        }
    }

    private fun sendWhatsAppReminder() {
        if (netBalance <= 0) {
            Toast.makeText(this, "This customer has no dues!", Toast.LENGTH_SHORT).show()
            return
        }
        ReminderDialog.show(
            context = this,
            customerName = customerName,
            customerPhone = customerPhone,
            balance = netBalance,
            lifecycleScope = lifecycleScope
        )
    }
    }
