

package com.example.finalprojectintern

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int,
    val amount: Int,
    val isGive: Boolean,
    val note: String,
    val date: String
)