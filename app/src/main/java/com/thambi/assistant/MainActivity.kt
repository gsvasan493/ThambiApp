package com.thambi.assistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.content.pm.PackageManager
import android.provider.AlarmClock
import java.util.Calendar

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
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")

        startActivityForResult(intent, REQUEST_CODE_SPEECH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""

            output.text = "You: $spokenText"

            val reply = getReply(spokenText)

            output.append("\nThambi: $reply")
            speak(reply)
        }
    }

    private fun getReply(input: String): String {
    val text = input.lowercase()

    return when {

        // 🔓 OPEN APPS
        text.contains("open youtube") -> {
            openApp("com.google.android.youtube")
            "Opening YouTube 🎬"
        }

        text.contains("open whatsapp") -> {
            openApp("com.whatsapp")
            "Opening WhatsApp 💬"
        }

        text.contains("open chrome") -> {
            openApp("com.android.chrome")
            "Opening Chrome 🌐"
        }

        // ⏰ SET ALARM
        text.contains("set alarm") -> {
            val numbers = text.split(" ").filter { it.toIntOrNull() != null }

            if (numbers.size >= 2) {
                val hour = numbers[0].toInt()
                val minute = numbers[1].toInt()

                setAlarm(hour, minute)
                "Alarm set for $hour:$minute ⏰"
            } else {
                "Tell time like 'set alarm 7 30'"
            }
        }

        // 🗣 NORMAL RESPONSES
        text.contains("hello") -> "Hello da 😄"
        text.contains("name") -> "I am Thambi Assistant 🤖"
        text.contains("time") -> Calendar.getInstance().time.toString()

        else -> "I heard you say $input"
    }
}
private fun openApp(packageName: String) {
    val pm = packageManager
    val intent = pm.getLaunchIntentForPackage(packageName)

    if (intent != null) {
        startActivity(intent)
    } else {
        speak("App not installed")
    }
}
private fun setAlarm(hour: Int, minute: Int) {
    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(AlarmClock.EXTRA_HOUR, hour)
        putExtra(AlarmClock.EXTRA_MINUTES, minute)
        putExtra(AlarmClock.EXTRA_MESSAGE, "Thambi Alarm")
    }

    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        speak("No alarm app found")
    }
}
    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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
