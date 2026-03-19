package com.thambi.assistant

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val input = findViewById<EditText>(R.id.userInput)
        val button = findViewById<Button>(R.id.sendButton)
        val output = findViewById<TextView>(R.id.outputText)
        val client = OkHttpClient()

button.setOnClickListener {
    val userText = input.text.toString()

    if (userText.isNotEmpty()) {
        output.text = "Thambi: thinking... 🤔"

        val json = JSONObject()
        json.put("model", "gpt-4.1-mini")

        val messages = org.json.JSONArray()
        val msg = JSONObject()
        msg.put("role", "user")
        msg.put("content", userText)
        messages.put(msg)

        json.put("messages", messages)

        val body = RequestBody.create(
            MediaType.get("application/json"),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer YOUR_API_KEY_HERE")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    output.text = "Thambi: Error 😢"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val jsonObj = JSONObject(responseData)

                val reply = jsonObj
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                runOnUiThread {
                    output.text = "Thambi: $reply"
                }
            }
        })
    }
}
    }
}
        
