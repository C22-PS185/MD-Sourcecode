package com.myteam.terapiin.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.myteam.terapiin.AboutApp
import com.myteam.terapiin.Help
import com.myteam.terapiin.PrivacyPolicy
import com.myteam.terapiin.R

class InfoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val aboutApp : TextView = view.findViewById(R.id.about_app)
        val privacyPolicy : TextView = view.findViewById(R.id.privacy_policy)
        val help : TextView = view.findViewById(R.id.bantuan)
        aboutApp.setOnClickListener{
            startActivity(Intent(activity,AboutApp::class.java))
        }
        privacyPolicy.setOnClickListener{
            startActivity(Intent(activity,PrivacyPolicy::class.java))
        }
        help.setOnClickListener{
            startActivity(Intent(activity,Help::class.java))
        }
    }
}
