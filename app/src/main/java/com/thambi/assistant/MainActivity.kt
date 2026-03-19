package com.thambi.assistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var output: TextView
    private lateinit var tts: TextToSpeech

    private val REQUEST_CODE_SPEECH = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val micButton = findViewById<Button>(R.id.micButton)
        output = findViewById(R.id.outputText)

        tts = TextToSpeech(this, this)

        micButton.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun startVoiceInput() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")

            startActivityForResult(intent, REQUEST_CODE_SPEECH)
        } catch (e: Exception) {
            output.text = "Voice not supported 😢"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH && resultCode == Activity.RESULT_OK) {

            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            if (result == null || result.isEmpty()) {
                output.text = "Didn't catch that 😅"
                return
            }

            val spokenText = result[0].lowercase().trim()

            output.text = "You: $spokenText"

            val reply = handleCommand(spokenText)

            output.append("\nThambi: $reply")
            speak(reply)
            output.append("\nDEBUG: trying to open app")
        }
    }

    // 🔥 MAIN COMMAND HANDLER
    private fun handleCommand(text: String): String {

        return try {

            // 📱 OPEN APPS
            when {
                text.contains("youtube") -> {
                    openApp("com.google.android.youtube")
                    "Opening YouTube"
                }

                text.contains("whatsapp") -> {
                    openApp("com.whatsapp")
                    "Opening WhatsApp"
                }

                text.contains("chrome") -> {
                    openApp("com.android.chrome")
                    "Opening Chrome"
                }

                // ⏰ SET ALARM
                text.contains("alarm") -> {
                    val numbers = Regex("\\d+").findAll(text).map { it.value.toInt() }.toList()

                    if (numbers.size >= 2) {
                        val hour = numbers[0]
                        val minute = numbers[1]

                        setAlarm(hour, minute)
                        "Setting alarm for $hour:$minute"
                    } else {
                        "Say time like 7 30"
                    }
                }

                // 🗣 NORMAL
                text.contains("hello") -> "Hello da 😄"
                text.contains("time") -> Date().toString()

                else -> "I heard: $text"
            }

        } catch (e: Exception) {
            "Something went wrong 😢"
        }
    }

    // 📱 OPEN APP SAFELY
    private fun openApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                startActivity(intent)
            } else {
                speak("App not installed")
            }
        } catch (e: Exception) {
            speak("Cannot open app")
        }
    }

    // ⏰ SET ALARM SAFELY
   private fun setAlarm(hour: Int, minute: Int) {
    try {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Thambi Alarm")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false) // important
        }

        startActivity(intent)

    } catch (e: Exception) {
        speak("Cannot set alarm")
    }
}

    // 🔊 TEXT TO SPEECH
    private fun speak(text: String) {
        if (::tts.isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}
