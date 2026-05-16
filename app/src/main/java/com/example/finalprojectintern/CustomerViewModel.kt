package com.example.finalprojectintern

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CustomerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val customerDao = db.customerDao()
    private val transactionDao = db.transactionDao()

    // All customers as Flow (auto-updates UI)
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()

    // Add new customer
    fun addCustomer(name: String, phone: String, notes: String = "") {
        viewModelScope.launch {
            customerDao.insertCustomer(
                Customer(name = name, phone = phone, notes = notes)
            )
        }
    }

    // Record a Give Credit transaction
    fun giveCredit(customer: Customer, amount: Int, note: String, date: String) {
        viewModelScope.launch {
            // Save transaction
            transactionDao.insertTransaction(
                Transaction(
                    customerId = customer.id,
                    amount = amount,
                    isGive = true,
                    note = note,
                    date = date
                )
            )
            // Update customer balance
            customerDao.updateCustomer(
                customer.copy(totalGiven = customer.totalGiven + amount)
            )
        }
    }

    // Record a Take Payment transaction
    fun takePayment(customer: Customer, amount: Int, note: String, date: String) {
        viewModelScope.launch {
            transactionDao.insertTransaction(
                Transaction(
                    customerId = customer.id,
                    amount = amount,
                    isGive = false,
                    note = note,
                    date = date
                )
            )
            customerDao.updateCustomer(
                customer.copy(totalTaken = customer.totalTaken + amount)
            )
        }
    }

    // Get transactions for a specific customer
    fun getTransactions(customerId: Int): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForCustomer(customerId)
    }

    // Delete customer
    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            customerDao.deleteCustomer(customer)
        }
    }
}