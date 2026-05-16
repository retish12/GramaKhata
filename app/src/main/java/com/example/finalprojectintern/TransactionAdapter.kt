package com.example.finalprojectintern

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class TransactionItem(
    val note: String,
    val date: String,
    val amount: Int,
    val isGive: Boolean // true = gave credit, false = took payment
)

class TransactionAdapter(
    private val transactions: List<TransactionItem>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dot: View = view.findViewById(R.id.viewDot)
        val tvNote: TextView = view.findViewById(R.id.tvNote)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val txn = transactions[position]
        holder.tvNote.text = txn.note
        holder.tvDate.text = txn.date

        if (txn.isGive) {
            holder.tvAmount.text = "+₹${txn.amount}"
            holder.tvAmount.setTextColor(0xFF1D9E75.toInt())
            holder.dot.setBackgroundColor(0xFF1D9E75.toInt())
        } else {
            holder.tvAmount.text = "-₹${txn.amount}"
            holder.tvAmount.setTextColor(0xFFE24B4A.toInt())
            holder.dot.setBackgroundColor(0xFFE24B4A.toInt())
        }
    }

    override fun getItemCount() = transactions.size
}
