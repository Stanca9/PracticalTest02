package com.example.practicaltest02v8

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
//import okhttp3.logging.HttpLoggingInterceptor

class calc : AppCompatActivity() {

    private val serverUrl = "http://10.0.2.2:8080" // Updated for Android emulator
    private val TAG = "calcActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calc)

        val editTextName: EditText = findViewById(R.id.editTextText)
        val editTextNumber1: EditText = findViewById(R.id.editTextNumberDecimal)
        val editTextNumber2: EditText = findViewById(R.id.editTextNumberDecimal2)
        val resultTextView: TextView = findViewById(R.id.textView2)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val t1 = editTextNumber1.text.toString().toIntOrNull()
                val t2 = editTextNumber2.text.toString().toIntOrNull()
                val operation = editTextName.text.toString().lowercase()

                if (t1 != null && t2 != null && operation.isNotEmpty()) {
                    fetchServerResponse(operation, t1, t2) { result ->
                        runOnUiThread {
                            resultTextView.text = result
                        }
                    }
                } else {
                    resultTextView.text = "Invalid input"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        editTextName.addTextChangedListener(textWatcher)
        editTextNumber1.addTextChangedListener(textWatcher)
        editTextNumber2.addTextChangedListener(textWatcher)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchServerResponse(operation: String, t1: Int, t2: Int, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient.Builder()
//                    .addInterceptor(HttpLoggingInterceptor().apply {
//                        level = HttpLoggingInterceptor.Level.BODY
//                    })
                    .build()

                val url = "$serverUrl?operation=$operation&t1=$t1&t2=$t2"
                Log.d(TAG, "Fetching URL: $url")

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "No response"
                    Log.d(TAG, "Server Response: $responseBody")
                    callback(responseBody.trim())
                } else {
                    Log.e(TAG, "Server returned error: ${response.code}")
                    callback("Error: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in server request: ${e.message}", e)
                callback("Error: ${e.message}")
            }
        }
    }
}
