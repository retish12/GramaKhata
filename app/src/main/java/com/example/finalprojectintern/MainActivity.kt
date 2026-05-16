package com.example.finalprojectintern

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: CustomerViewModel by viewModels()
    private lateinit var adapter: CustomerAdapter
    private lateinit var tvGiven: TextView
    private lateinit var tvDue: TextView

    private val addCustomerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val name = result.data?.getStringExtra("name") ?: return@registerForActivityResult
            val phone = result.data?.getStringExtra("phone") ?: ""
            val notes = result.data?.getStringExtra("notes") ?: ""
            viewModel.addCustomer(name, phone, notes)
            Toast.makeText(this, "✅ $name added!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCustomers)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddCustomer)
        tvGiven = findViewById(R.id.tvTotalGiven)
        tvDue = findViewById(R.id.tvTotalDue)

        
        findViewById<TextView>(R.id.btnReport).setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        // Set up adapter
        adapter = CustomerAdapter(mutableListOf()) { customer ->
            openCustomerDetail(customer)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observe database — UI updates automatically!
        lifecycleScope.launch {
            viewModel.allCustomers.collect { customers ->
                val items = customers.map { c ->
                    CustomerItem(
                        name = c.name,
                        phone = c.phone,
                        balance = c.netBalance,
                        id = c.id,
                        totalGiven = c.totalGiven,
                        totalTaken = c.totalTaken
                    )
                }
                updateAdapter(items)

                // Update totals
                val totalDue = customers.sumOf { it.netBalance }
                val totalGiven = customers.sumOf { it.totalGiven }
                tvGiven.text = "\u20B9$totalGiven"
                tvDue.text = "\u20B9$totalDue"
            }
        }

        // FAB - Add Customer
        fabAdd.setOnClickListener {
            addCustomerLauncher.launch(Intent(this, AddCustomerActivity::class.java))
        }
    }

    private fun openCustomerDetail(customer: CustomerItem) {
        val intent = Intent(this, CustomerDetailActivity::class.java)
        intent.putExtra("customerId", customer.id)
        intent.putExtra("name", customer.name)
        intent.putExtra("phone", customer.phone)
        intent.putExtra("balance", customer.balance)
        intent.putExtra("totalGiven", customer.totalGiven)
        intent.putExtra("totalTaken", customer.totalTaken)
        startActivity(intent)
    }

    private fun updateAdapter(newList: List<CustomerItem>) {
        val mutableList = newList.toMutableList()
        adapter = CustomerAdapter(mutableList) { customer ->
            openCustomerDetail(customer)
        }
        findViewById<RecyclerView>(R.id.recyclerViewCustomers).adapter = adapter
    }
}