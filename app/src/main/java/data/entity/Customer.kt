package com.example.finalprojectintern

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phone: String,
    val notes: String = "",
    val totalGiven: Int = 0,
    val totalTaken: Int = 0
) {
    // Net balance = how much customer owes you
    val netBalance: Int get() = totalGiven - totalTaken
}