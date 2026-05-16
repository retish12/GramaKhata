package com.example.finalprojectintern

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object OpenAIService {

    private const val API_KEY = "AIzaSyAvdvR-J0n7SI4_JCWfKA0-IEXsC_kuQiE"
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$API_KEY"

    suspend fun generateReminder(
        customerName: String,
        balance: Int,
        shopName: String
    ): String = withContext(Dispatchers.IO) {
        try {
            callGemini(customerName, balance, shopName)
        } catch (ex: Exception) {
            fallbackMessage(customerName, balance, shopName)
        }
    }

    private fun callGemini(
        customerName: String,
        balance: Int,
        shopName: String
    ): String {
        val url = URL(API_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 15000
        connection.readTimeout = 30000

        val prompt = "Generate a polite and friendly payment reminder message " +
                "for a customer named $customerName who owes Rs.$balance at $shopName. " +
                "Start with Namaskar, be warm and respectful, mention the exact amount, " +
                "ask them to pay at their convenience, end with thank you. " +
                "Keep it under 80 words. Indian style greeting."

        val part = JSONObject()
        part.put("text", prompt)

        val partsArray = JSONArray()
        partsArray.put(part)

        val content = JSONObject()
        content.put("parts", partsArray)

        val contentsArray = JSONArray()
        contentsArray.put(content)

        val body = JSONObject()
        body.put("contents", contentsArray)

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(body.toString())
        writer.flush()
        writer.close()

        return if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            val json = JSONObject(response.toString())
            json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()
        } else {
            fallbackMessage(customerName, balance, shopName)
        }
    }

    fun fallbackMessage(
        customerName: String,
        balance: Int,
        shopName: String
    ): String {
        return "Namaskar $customerName Ji,\n\n" +
                "Hope you are doing well!\n\n" +
                "This is a gentle reminder that your " +
                "outstanding balance at $shopName is Rs.$balance.\n\n" +
                "Kindly make the payment at your earliest convenience.\n\n" +
                "Thank you for your trust and support!"
    }
}