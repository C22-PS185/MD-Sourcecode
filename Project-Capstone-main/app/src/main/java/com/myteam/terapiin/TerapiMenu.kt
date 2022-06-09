package com.myteam.terapiin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.myteam.terapiin.databinding.ActivityTerapiMenuBinding

class TerapiMenu : AppCompatActivity() {
    private lateinit var binding: ActivityTerapiMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTerapiMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.camOpenMouth.setOnClickListener{
            val intent = Intent(this@TerapiMenu,CameraPage::class.java)
            intent.putExtra("openmouth",1)
            startActivity(intent)
        }
        binding.camPuffCheek.setOnClickListener{
            val intent = Intent(this@TerapiMenu,CameraPage::class.java)
            intent.putExtra("puffcheek",2)
            startActivity(intent)
        }
        binding.camShowTeeth.setOnClickListener{
            val intent = Intent(this@TerapiMenu,CameraPage::class.java)
            intent.putExtra("showteeth",3)
            startActivity(intent)
        }
        binding.camSmile.setOnClickListener{
            val intent = Intent(this@TerapiMenu,CameraPage::class.java)
            intent.putExtra("smile",4)
            startActivity(intent)
        }
        binding.camSneer.setOnClickListener{
            val intent = Intent(this@TerapiMenu,CameraPage::class.java)
            intent.putExtra("sneer",5)
            startActivity(intent)
        }

        val buttonKembali : Button = findViewById(R.id.button_kembali)
        buttonKembali.setOnClickListener{
            startActivity(Intent(this@TerapiMenu,MenuPage::class.java))
        }
    }

}