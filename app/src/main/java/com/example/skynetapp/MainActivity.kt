package com.example.skynetapp

import android.content.DialogInterface
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class MainActivity : AppCompatActivity() {
    var t1: TextToSpeech? = null
    var ed1: EditText? = null
    var b1: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tts_main)

        ed1 = findViewById<View>(R.id.editText) as EditText
        b1 = findViewById<View>(R.id.button) as Button

        t1 = TextToSpeech(applicationContext,
            OnInitListener { status ->
                if (status != TextToSpeech.ERROR) {
                    t1!!.language = Locale.UK
                }
            })

        b1?.setOnClickListener()
        {
            val toSpeak = ed1!!.text.toString()
            Toast.makeText(applicationContext, toSpeak, Toast.LENGTH_SHORT).show()
            t1!!.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    override fun onPause() {
        if (t1 != null) {
            t1!!.stop()
            t1!!.shutdown()
        }
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}

private fun Button.setOnClickListener(onClickListener: DialogInterface.OnClickListener) {

}
