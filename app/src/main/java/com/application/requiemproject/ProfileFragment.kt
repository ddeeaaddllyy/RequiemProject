package com.application.requiemproject

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoutButton = view.findViewById<Button>(R.id.button_logout)
        val settingsButton = view.findViewById<Button>(R.id.button_settings)
        val usernameText = view.findViewById<TextView>(R.id.text_username)

        usernameText.text = "Unknown"

        logoutButton.setOnClickListener {
            Toast.makeText(requireContext(), "Выход из системы...", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        settingsButton.setOnClickListener {
            Toast.makeText(requireContext(), "Настройки", Toast.LENGTH_SHORT).show()
        }
    }
}