

package com.example.finalprojectintern

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class CustomerItem(
    val name: String,
    val phone: String,
    val balance: Int,
    val id: Int = 0,
    val totalGiven: Int = 0,
    val totalTaken: Int = 0
) {
    val netBalance: Int get() = balance
}

class CustomerAdapter(
    private val customers: List<CustomerItem>,
    private val onClick: (CustomerItem) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val tvName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val tvBalance: TextView = view.findViewById(R.id.tvBalance)
        val tvLabel: TextView = view.findViewById(R.id.tvDueLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customer = customers[position]

        // Show first letter as avatar
        holder.tvAvatar.text = customer.name.first().uppercase()

        holder.tvName.text = customer.name
        holder.tvPhone.text = customer.phone

        if (customer.balance == 0) {
            holder.tvBalance.text = "Settled"
            holder.tvBalance.setTextColor(0xFF1D9E75.toInt())
            holder.tvLabel.text = "all clear"
        } else {
            holder.tvBalance.text = "₹${customer.balance}"
            holder.tvBalance.setTextColor(0xFFE24B4A.toInt())
            holder.tvLabel.text = "owes you"
        }

        holder.itemView.setOnClickListener { onClick(customer) }
    }

    override fun getItemCount() = customers.size
}