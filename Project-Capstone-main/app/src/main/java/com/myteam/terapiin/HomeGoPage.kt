package com.myteam.terapiin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class HomeGoPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_go_page)

        val btnGo : Button = findViewById(R.id.id_buttongo)
        btnGo.setOnClickListener{
            startActivity(Intent(this@HomeGoPage,MenuPage::class.java))
        }
    }
}