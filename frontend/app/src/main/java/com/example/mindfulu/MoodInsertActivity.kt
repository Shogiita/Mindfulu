package com.example.mindfulu

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton // Change Button to ImageButton for emojis
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mindfulu.ui.HomeActivity

class MoodInsertActivity : AppCompatActivity() {
    var MoodSelected: String = ""
    private lateinit var SadEmoji: ImageButton
    private lateinit var BadEmoji: ImageButton
    private lateinit var HappyEmoji: ImageButton
    private lateinit var LovelyEmoji: ImageButton
    private lateinit var SubmitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mood_insert)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SadEmoji = findViewById(R.id.SadEmoji)
        BadEmoji = findViewById(R.id.BadEmoji)
        HappyEmoji = findViewById(R.id.HappyEmoji)
        LovelyEmoji = findViewById(R.id.LovelyEmoji)
        SubmitButton = findViewById(R.id.SubmitButton)

        SadEmoji.setOnClickListener {
            MoodSelected = "Sad"
            SadEmoji.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00FF00"))
            BadEmoji.backgroundTintList = null
            HappyEmoji.backgroundTintList = null
            LovelyEmoji.backgroundTintList = null
            Toast.makeText(this, "SAD", Toast.LENGTH_SHORT).show()
        }

        BadEmoji.setOnClickListener {
            MoodSelected = "Bad"
        }

        HappyEmoji.setOnClickListener {
            MoodSelected = "Happy"
        }

        LovelyEmoji.setOnClickListener {
            MoodSelected = "Lovely"

        }

        SubmitButton.setOnClickListener {
            if(MoodSelected != ""){
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Mood must be selected", Toast.LENGTH_SHORT).show()
            }
        }
    }
}