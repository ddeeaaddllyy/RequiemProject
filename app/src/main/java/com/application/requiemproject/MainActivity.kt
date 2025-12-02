package com.application.requiemproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val buttonLogin = findViewById<Button>(R.id.button_login)
        val buttonRegister = findViewById<Button>(R.id.button_register)
        val inputLogin = findViewById<TextInputEditText>(R.id.input_login)
        val inputPassword = findViewById<TextInputEditText>(R.id.input_password)

        buttonLogin.setOnClickListener {
            val enteredEmail = inputLogin.text.toString().trim()
            val enteredPassword = inputPassword.text.toString().trim()

            if (enteredPassword == "asd" && enteredEmail == "gg@mail.ru"){
                Toast.makeText(this, "correct", Toast.LENGTH_LONG).show()
            } else{
                Toast.makeText(this, "incorrect", Toast.LENGTH_LONG).show()
            }

        }

        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


    }

}