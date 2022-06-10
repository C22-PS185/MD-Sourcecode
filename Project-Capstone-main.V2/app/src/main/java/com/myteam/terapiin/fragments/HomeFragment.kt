package com.myteam.terapiin.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.myteam.terapiin.R
import com.myteam.terapiin.TerapiMenu
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        terapi_camera.setOnClickListener{
            startActivity(Intent(activity,TerapiMenu::class.java))
        }
        terapiFisik.setOnClickListener{
            Toast.makeText(activity,"Fitur Masih Dalam Proses Pengembangan",Toast.LENGTH_SHORT).show()
        }
        terapiSuara.setOnClickListener{
            Toast.makeText(activity,"Fitur Masih Dalam Proses Pengembangan",Toast.LENGTH_SHORT).show()
        }
    }
}