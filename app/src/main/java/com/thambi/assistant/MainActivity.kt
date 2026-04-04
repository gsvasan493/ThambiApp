package com.thambi.assistant

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.provider.ContactsContract
import android.widget.Toast
import android.widget.Button
import android.widget.TextView
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var output: TextView
    private lateinit var tts: TextToSpeech

    private val REQUEST_CODE_SPEECH = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS)
    != PackageManager.PERMISSION_GRANTED) {

    requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), 1)
}

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

        if (result.isNullOrEmpty()) {
            output.text = "Didn't catch that 😅"
            return
        }

        val spokenText = result[0].lowercase().trim()

        if (spokenText.length < 3) {
            output.text = "Didn't catch that 😅"
            return
        }

        // 🔥 WAKE WORD CHECK
        if (!spokenText.startsWith("hey thambi") && 
    !spokenText.startsWith("dai thambi") &&
    !spokenText.startsWith("thambi")) {
            output.text = "Say 'Dai Thambi' first 😄"
            return
        }

        // 🔥 REMOVE WAKE WORD
       val cleanText = spokenText
    .replace("hey thambi", "")
    .replace("dai thambi", "")
    .replace("thambi", "")
    .trim()

        output.text = "You: $spokenText\nListening..."

        val reply = handleCommand(cleanText)

        output.append("\nThambi: $reply")
        speak(reply)
    }
}

    // 🔥 MAIN COMMAND HANDLER
   private fun handleCommand(text: String): String {

    return try {

        when {

            // 📱 OPEN ANY APP
            text.contains("open") -> {
                val appName = text.replace("open", "").trim()

                val success = openAnyApp(appName)

                if (success) {
                    "Opening $appName"
                } else {
                    "App not found"
                }
            }
            
            
           text.contains("play") -> {
    val song = text.replace("play", "").trim()

    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://www.youtube.com/results?search_query=$song")
    }

    startActivity(intent)

    "Playing $song"
}

   
            // Tamil open app


            // ⏰ SET ALARM
            text.contains("multiple alarms") -> {

    setAlarm(6, 0)
    setAlarm(7, 0)
    setAlarm(8, 0)

    "Setting multiple alarms for 6, 7, and 8 AM"
}
            text.contains("cancel alarm") -> {

    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
    startActivity(intent)

    "Opening alarms. Please turn it off"
}

  (text.contains("set") && text.contains("alarm")) ||
(text.contains("wake") && text.contains("at")) -> {

    // ⏳ AFTER MINUTES (e.g., "after 10 minutes")
    if (text.contains("after") && text.contains("minute")) {

        val minutes = Regex("\\d+").find(text)?.value?.toIntOrNull()

        if (minutes != null) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, minutes)

            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            setAlarm(hour, minute)

            return "Setting alarm after $minutes minutes"
        } else {
            return "Tell how many minutes"
        }
    }

    // 🕒 NORMAL TIME (6 AM, 7:30 PM)
    val regex = Regex("(\\d{1,2})(:?)(\\d{0,2})\\s?(am|pm)?")
    val match = regex.find(text)

    if (match != null) {

        var hour = match.groupValues[1].toInt()

        val minute = if (match.groupValues[3].isNotEmpty()) {
            match.groupValues[3].toInt()
        } else 0

        val amPm = match.groupValues[4]

        if (amPm == "pm" && hour < 12) hour += 12
        if (amPm == "am" && hour == 12) hour = 0

        setAlarm(hour, minute)

        "Setting alarm for $hour:${minute.toString().padStart(2, '0')}"
    } else {
        "Tell time clearly like 6 AM or 7:30 PM"
    }
}
text.contains("call") -> {
    val name = text.replace("call", "").trim()

    val success = callContact(name)

    if (success) "Calling $name"
    else "Contact not found"
}
text.contains("whatsapp") -> {

    val words = text.split(" ")

    if (!words.contains("to") || words.indexOf("to") == words.size - 1) {
        return "Say: whatsapp to name message"
    }

    val afterTo = text.substringAfter("to").trim()
val parts = afterTo.split(" ", limit = 2)

if (parts.size < 2) {
    return "Say: whatsapp to name message"
}

val name = parts[0]
val message = parts[1]
}

    sendWhatsAppToContact(name, message)

    "Sending message to $name"
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
   private fun callContact(name: String): Boolean {
    val resolver = contentResolver
    val cursor = resolver.query(
        android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null
    )

    cursor?.use {
        while (it.moveToNext()) {
            val contactName = it.getString(
                it.getColumnIndexOrThrow(
                    android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
            ).lowercase()

            val number = it.getString(
                it.getColumnIndexOrThrow(
                    android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                )
            )

            if (contactName.contains(name.lowercase()) ||
    name.lowercase().contains(contactName)) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$number")
                startActivity(intent)
                return true
            }
        }
    }
    return false
}
  private fun getContactNumber(name: String): String? {

    val cursor = contentResolver.query(
        android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null, null, null, null
    )

    cursor?.use {
        while (it.moveToNext()) {

            val contactName = it.getString(
                it.getColumnIndexOrThrow(
                    android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
            ).lowercase()

            val number = it.getString(
                it.getColumnIndexOrThrow(
                    android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                )
            )

            if (contactName.lowercase().contains(name.lowercase())) {

                val cleanNumber = number.replace(" ", "").replace("+", "")

                return if (cleanNumber.startsWith("91")) cleanNumber
                else "91$cleanNumber"
            }
        }
    }

    return null
}
private fun openAnyApp(appName: String): Boolean {
    val pm = packageManager
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)

    val apps = pm.queryIntentActivities(intent, 0)

    for (app in apps) {
        val label = app.loadLabel(pm).toString().lowercase()

        if (label.contains(appName)) {
            val launchIntent = pm.getLaunchIntentForPackage(app.activityInfo.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
                return true
            }
        }
    }
    return false
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

        output.append("\nDEBUG: Setting alarm $hour:$minute")

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

    } catch (e: Exception) {
        output.append("\nERROR: ${e.message}")
        speak("Cannot set alarm")
    }
}
  fun sendWhatsAppToContact(name: String, message: String) {
    val phoneNumber = getPhoneNumberFromName(name)

    if (phoneNumber != null) {
        val uri = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    } else {
        Toast.makeText(this, "Contact not found", Toast.LENGTH_SHORT).show()
    }
}
  
  
  private fun playMusic() {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_APP_MUSIC)

    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        speak("No music app found")
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
