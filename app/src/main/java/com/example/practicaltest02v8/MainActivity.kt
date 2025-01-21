package com.example.practicaltest02v8

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val CACHE_EXPIRY_TIME = 6_000 // 1 minute in milliseconds
    }

    // Cache variables
    private var cachedEuroRate: String? = null
    private var cachedUsdRate: String? = null
    private var lastCacheUpdateTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize the views
        val editTextNumber: EditText = findViewById(R.id.editText)
        val textView: TextView = findViewById(R.id.textView)
        val button: Button = findViewById(R.id.button)

        // Set up button click listener
        button.setOnClickListener {
            val inputCurrency = editTextNumber.text.toString().trim().uppercase() // Get input and normalize it
            if (inputCurrency != "EUR" && inputCurrency != "USD") {
                runOnUiThread {
                    textView.text = "Please enter 'EUR' or 'USD'"
                }
                Log.d(TAG, "Invalid input currency: $inputCurrency")
                return@setOnClickListener
            }

            Log.d(TAG, "Button clicked with input: $inputCurrency")
            CoroutineScope(Dispatchers.IO).launch {
                val (euroRate, usdRate) = getBitcoinPrices()
                Log.d(TAG, "Bitcoin Prices: EUR = $euroRate, USD = $usdRate")

                // Show only the relevant rate based on user input
                val displayRate = when (inputCurrency) {
                    "EUR" -> euroRate
                    "USD" -> usdRate
                    else -> "Invalid currency" // This should never happen
                }

                runOnUiThread {
                    textView.text = "$inputCurrency Rate: $displayRate"
                }
            }
        }

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Function to get Bitcoin prices (with caching)
    private suspend fun getBitcoinPrices(): Pair<String, String> {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCacheUpdateTime <= CACHE_EXPIRY_TIME && cachedEuroRate != null && cachedUsdRate != null) {
            Log.d(TAG, "Using cached values.")
            return Pair(cachedEuroRate!!, cachedUsdRate!!)
        }

        Log.d(TAG, "Cache expired or empty. Fetching new values from server.")
        val response = fetchBitcoinPrice()
        return if (response != null) {
            val euroRate = response.bpi?.EUR?.rate ?: "N/A"
            val usdRate = response.bpi?.USD?.rate ?: "N/A"

            // Update cache
            cachedEuroRate = euroRate
            cachedUsdRate = usdRate
            lastCacheUpdateTime = currentTime

            Pair(euroRate, usdRate)
        } else {
            Pair("Error", "Error")
        }
    }

    // Function to fetch data from the API
    private fun fetchBitcoinPrice(): ExampleJson2KtKotlin? {
        val gson = Gson() // Create Gson instance
        return try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.coindesk.com/v1/bpi/currentprice/EUR.json")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response JSON: $responseBody")
                // Parse the JSON string into ApiResponse
                gson.fromJson(responseBody, ExampleJson2KtKotlin::class.java)
            } else {
                Log.e(TAG, "API request failed with code: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API request: ${e.message}", e)
            null
        }
    }
}
