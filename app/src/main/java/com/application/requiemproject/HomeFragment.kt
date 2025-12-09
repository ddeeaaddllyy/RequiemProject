package com.application.requiemproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var projectionManager: MediaProjectionManager
    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            startBackgroundWork(result.resultCode, result.data!!)
        } else {
            Toast.makeText(requireContext(), "В доступе отказано", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLanguageDropdowns(view)

        projectionManager = requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val startButton = view.findViewById<Button>(R.id.button_start_translation)
        startButton.setOnClickListener {
            requestScreenCapture()
        }
    }

    private fun setupLanguageDropdowns(view: View) {
        val languages = listOf("English", "Russian", "Japanese", "German", "French")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languages)

        val sourceInput = view.findViewById<AutoCompleteTextView>(R.id.input_source_lang)
        val targetInput = view.findViewById<AutoCompleteTextView>(R.id.input_target_lang)

        sourceInput.setAdapter(adapter)
        targetInput.setAdapter(adapter)
    }

    private fun requestScreenCapture() {
        val captureIntent = projectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }

    private fun startBackgroundWork(resultCode: Int, data: Intent) {
        Toast.makeText(requireContext(), "Запуск в фоне...", Toast.LENGTH_SHORT).show()

         val serviceIntent = Intent(requireContext(), ScreenCaptureService::class.java)
         serviceIntent.putExtra("RESULT_CODE", resultCode)
         serviceIntent.putExtra("DATA", data)
         requireContext().startForegroundService(serviceIntent)

        minimizeApp()
    }

    private fun minimizeApp() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }
}