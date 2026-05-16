package com.example.finalprojectintern

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {

    private val viewModel: CustomerViewModel by viewModels()
    private lateinit var tvAiInsight: TextView
    private var customerList: List<Customer> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // Set date
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
        findViewById<TextView>(R.id.tvDate).text = dateFormat.format(Date())

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        tvAiInsight = findViewById(R.id.tvAiInsight)

        // Observe customers
        lifecycleScope.launch {
            viewModel.allCustomers.collect { customers ->
                customerList = customers
                updateReport(customers)
            }
        }

        // Refresh AI insight button
        findViewById<Button>(R.id.btnRefreshInsight).setOnClickListener {
            generateAiInsight()
        }
    }

    private fun updateReport(customers: List<Customer>) {
        val totalGiven = customers.sumOf { it.totalGiven }
        val totalTaken = customers.sumOf { it.totalTaken }
        val netDue = customers.sumOf { it.netBalance }

        // Update summary cards
        findViewById<TextView>(R.id.tvTotalGiven).text = "\u20B9$totalGiven"
        findViewById<TextView>(R.id.tvTotalReceived).text = "\u20B9$totalTaken"
        findViewById<TextView>(R.id.tvNetDue).text = "\u20B9$netDue"

        // Top dues
        val topDues = customers.filter { it.netBalance > 0 }
            .sortedByDescending { it.netBalance }
            .take(5)
        updateTopDues(topDues, netDue)

        // All customers summary
        updateAllCustomers(customers)

        // Generate AI insight
        generateAiInsight()
    }

    private fun updateTopDues(customers: List<Customer>, maxDue: Int) {
        val layout = findViewById<LinearLayout>(R.id.layoutTopDues)
        layout.removeAllViews()

        if (customers.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No outstanding dues! All customers are settled."
            tv.textSize = 13f
            tv.setTextColor(Color.parseColor("#1D9E75"))
            layout.addView(tv)
            return
        }

        customers.forEach { customer ->
            // Customer name row
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = 12
            row.layoutParams = params

            // Name
            val tvName = TextView(this)
            tvName.text = customer.name
            tvName.textSize = 13f
            tvName.setTextColor(Color.parseColor("#212121"))
            val nameParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            tvName.layoutParams = nameParams

            // Progress bar
            val progressBar = ProgressBar(this,
                null, android.R.attr.progressBarStyleHorizontal)
            val progress = if (maxDue > 0)
                (customer.netBalance * 100 / maxDue) else 0
            progressBar.progress = progress
            progressBar.progressDrawable.setTint(Color.parseColor("#E24B4A"))
            val pbParams = LinearLayout.LayoutParams(0,
                20, 1.5f)
            pbParams.marginStart = 8
            pbParams.marginEnd = 8
            progressBar.layoutParams = pbParams

            // Amount
            val tvAmount = TextView(this)
            tvAmount.text = "\u20B9${customer.netBalance}"
            tvAmount.textSize = 13f
            tvAmount.setTextColor(Color.parseColor("#E24B4A"))
            tvAmount.typeface = android.graphics.Typeface.DEFAULT_BOLD

            row.addView(tvName)
            row.addView(progressBar)
            row.addView(tvAmount)
            layout.addView(row)
        }
    }

    private fun updateAllCustomers(customers: List<Customer>) {
        val layout = findViewById<LinearLayout>(R.id.layoutAllCustomers)
        layout.removeAllViews()

        if (customers.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No customers yet. Add customers from home screen."
            tv.textSize = 13f
            tv.setTextColor(Color.parseColor("#9E9E9E"))
            layout.addView(tv)
            return
        }

        customers.forEach { customer ->
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = 12
            row.layoutParams = params

            // Avatar
            val avatar = TextView(this)
            avatar.text = customer.name.first().uppercase()
            avatar.textSize = 14f
            avatar.setTextColor(Color.WHITE)
            avatar.setBackgroundColor(Color.parseColor("#1D9E75"))
            avatar.gravity = Gravity.CENTER
            val avatarParams = LinearLayout.LayoutParams(40, 40)
            avatarParams.marginEnd = 12
            avatar.layoutParams = avatarParams

            // Name and phone
            val info = LinearLayout(this)
            info.orientation = LinearLayout.VERTICAL
            val infoParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            info.layoutParams = infoParams

            val tvName = TextView(this)
            tvName.text = customer.name
            tvName.textSize = 14f
            tvName.setTextColor(Color.parseColor("#212121"))

            val tvPhone = TextView(this)
            tvPhone.text = customer.phone
            tvPhone.textSize = 11f
            tvPhone.setTextColor(Color.parseColor("#9E9E9E"))

            info.addView(tvName)
            info.addView(tvPhone)

            // Balance
            val tvBalance = TextView(this)
            if (customer.netBalance <= 0) {
                tvBalance.text = "Settled"
                tvBalance.setTextColor(Color.parseColor("#1D9E75"))
            } else {
                tvBalance.text = "\u20B9${customer.netBalance}"
                tvBalance.setTextColor(Color.parseColor("#E24B4A"))
            }
            tvBalance.textSize = 14f
            tvBalance.typeface = android.graphics.Typeface.DEFAULT_BOLD

            row.addView(avatar)
            row.addView(info)
            row.addView(tvBalance)
            layout.addView(row)
        }
    }

    private fun generateAiInsight() {
        tvAiInsight.text = "Generating insight..."

        lifecycleScope.launch {
            try {
                val totalGiven = customerList.sumOf { it.totalGiven }
                val totalTaken = customerList.sumOf { it.totalTaken }
                val netDue = customerList.sumOf { it.netBalance }
                val topDebtor = customerList.maxByOrNull { it.netBalance }
                val settledCount = customerList.count { it.netBalance <= 0 }
                val totalCustomers = customerList.size

                val url = java.net.URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyAvdvR-J0n7SI4_JCWfKA0-IEXsC_kuQiE")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 30000

                val prompt = "You are a business advisor for a small Indian shopkeeper. " +
                        "Here is today's business summary: " +
                        "Total credit given to customers: Rs.$totalGiven, " +
                        "Total payments received: Rs.$totalTaken, " +
                        "Total outstanding dues: Rs.$netDue, " +
                        "Total customers: $totalCustomers, " +
                        "Settled customers today: $settledCount" +
                        "${if (topDebtor != null && topDebtor.netBalance > 0) ", Highest due customer: ${topDebtor.name} owes Rs.${topDebtor.netBalance}" else ""}. " +
                        "Give exactly 2-3 sentences of practical business insight and one actionable tip. " +
                        "Be encouraging, specific, and practical. Maximum 60 words total."

                val part = org.json.JSONObject()
                part.put("text", prompt)
                val partsArray = org.json.JSONArray()
                partsArray.put(part)
                val content = org.json.JSONObject()
                content.put("parts", partsArray)
                val contentsArray = org.json.JSONArray()
                contentsArray.put(content)
                val body = org.json.JSONObject()
                body.put("contents", contentsArray)

                val writer = java.io.OutputStreamWriter(connection.outputStream)
                writer.write(body.toString())
                writer.flush()
                writer.close()

                if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    val reader = java.io.BufferedReader(
                        java.io.InputStreamReader(connection.inputStream)
                    )
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val json = org.json.JSONObject(response.toString())
                    val insight = json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                        .trim()

                    tvAiInsight.text = insight
                } else {
                    tvAiInsight.text = "You have $totalCustomers customers with " +
                            "Rs.$netDue total outstanding. " +
                            "Tip: Send reminders to customers who haven't paid in over a week!"
                }
            } catch (ex: Exception) {
                tvAiInsight.text = "You have ${customerList.size} customers tracked. " +
                        "Net outstanding: Rs.${customerList.sumOf { it.netBalance }}. " +
                        "Tip: Follow up with customers who have the highest dues first!"
            }
        }
    }
}