package com.example.appprofiles

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.appprofiles.Interfaces.ApiInterface
import com.example.appprofiles.Models.ResponseUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            login(
                findViewById<EditText>(R.id.et_usr).text,
                findViewById<EditText>(R.id.et_pwd).text
            )


        }
    }

    private fun login(usr: CharSequence?, pwd: CharSequence?) {
        if (usr.toString() == "admin" && pwd.toString() == "admin") {
            Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, UsrList::class.java))
            finish()
        } else {
            val apiInterface = ApiInterface.create().login(usr.toString(), pwd.toString())


            apiInterface.enqueue(object : Callback<ResponseUser> {  //Pone en cola las solicitudes
                override fun onResponse(
                    call: Call<ResponseUser>?,
                    response: Response<ResponseUser>?
                ) {

                    if (response?.body() != null) {                //Valida si la respuesta del servicio es != null

                        if (response.body()!!.result) {           //Valida que el servicio haya respondido la propiedad result en True
                            val user = response.body()!!.records
                            Toast.makeText(applicationContext, "Login exitoso", Toast.LENGTH_SHORT)
                                .show()
                            startActivity(
                                Intent(                         //Iniciar la nueva actividad(Clase Profile, vista activity_profile)
                                    applicationContext,
                                    Profile::class.java
                                ).putExtra("id", user.id)
                            )
                            finish()                           //terminar Main Activity
                        } else
                            Toast.makeText(
                                applicationContext,
                                response.body()!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                    } else
                        Toast.makeText(applicationContext, "Credenciales invalidas", Toast.LENGTH_SHORT)
                            .show()

                }

                override fun onFailure(call: Call<ResponseUser>?, t: Throwable?) {

                    Toast.makeText(applicationContext, "Ocurrio un error al conectar con el servidor", Toast.LENGTH_SHORT)
                        .show()
                }
            })


        }

    }
}