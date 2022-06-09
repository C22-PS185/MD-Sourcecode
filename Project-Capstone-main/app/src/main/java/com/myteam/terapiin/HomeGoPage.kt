package com.myteam.terapiin

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.home_go_page.*

class HomeGoPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_go_page)

        playAnimation()

        val btnGo : Button = findViewById(R.id.id_buttongo)
        btnGo.setOnClickListener{
            startActivity(Intent(this@HomeGoPage,MenuPage::class.java))
        }

    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(terapiinImage, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
    }
}