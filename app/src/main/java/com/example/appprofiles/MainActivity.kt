package com.example.appprofiles

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            login(
                findViewById<android.widget.EditText>(R.id.et_usr).text,
                findViewById<android.widget.EditText>(R.id.et_pwd).text
            )


        }
    }

    private fun login(usr: CharSequence?, pwd: CharSequence?) {
    if (usr.toString()=="admin"&& pwd.toString()=="admin")
    {
        Toast.makeText(this,"Login exitoso",Toast.LENGTH_SHORT).show()
        startActivity(Intent(this,UsrList::class.java))
        finish()
    }
    else
        Toast.makeText(this,"Credenciales invalidas",Toast.LENGTH_SHORT).show()
    }
}