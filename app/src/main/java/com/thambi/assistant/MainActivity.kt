package com.thambi.assistant

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val input = findViewById<EditText>(R.id.userInput)
        val button = findViewById<Button>(R.id.sendButton)
        val output = findViewById<TextView>(R.id.outputText)

        button.setOnClickListener {
            val userText = input.text.toString()

            if (userText.isNotEmpty()) {
                output.text = "Thambi: You said -> $userText"
            } else {
                output.text = "Thambi: Say something da 😄"
            }
        }
    }
}
