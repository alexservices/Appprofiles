package com.example.appprofiles

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appprofiles.Interfaces.ApiInterface
import com.example.appprofiles.Models.PuestoLaboral
import com.example.appprofiles.Models.ResponseUser
import com.example.appprofiles.Models.ResponseUsers
import com.example.appprofiles.Models.User
import com.example.appprofiles.adapters.AdapterPerfiles
import com.example.appprofiles.adapters.AdapterWorks
import com.example.appprofiles.utils.Utils
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader

class UsrList : AppCompatActivity() {
    companion object {
        const val PERMISSION_CODE = 1
        const val READ_FILE_CODE = 2
    }

    var listworks = ArrayList<PuestoLaboral>()
    val user = User()
    lateinit var btnCrear: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usr_list)
        Utils.checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_CODE, this)
        findViewById<Button>(R.id.btn_file).setOnClickListener {
            chooseFile()
        }
        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            startActivity(
                Intent(                         //Iniciar la nueva actividad(Clase Profile, vista activity_profile)
                    this,
                    MainActivity::class.java
                )
            )
            finish()
        }
        btnCrear = findViewById(R.id.btn_crear)
        btnCrear.isEnabled = false
        btnCrear.setOnClickListener {
            createProfile(user, createJsonWorks().toString())
        }
    }

    private fun getData() {
        val apiInterface = ApiInterface.create().listar_usuarios()


        apiInterface.enqueue(object : Callback<ResponseUsers> {  //Pone en cola las solicitudes
            override fun onResponse(
                call: Call<ResponseUsers>?,
                response: Response<ResponseUsers>?
            ) {
                if (response?.body() != null) {                //Valida si la respuesta del servicio es != null
                    if (response.body()!!.result) {
                        var list: RecyclerView = findViewById(R.id.rv_list)
                        list.setHasFixedSize(true)
                        list.layoutManager = LinearLayoutManager(this@UsrList)
                        list.adapter = AdapterPerfiles(response.body()!!.records, this@UsrList)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseUsers>, t: Throwable) {
                Toast.makeText(this@UsrList, "Ocurrio un error conectandose al servidor", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun createJsonWorks(): JSONArray {
        val array = JSONArray()
        for (work in listworks) {
            val obj = JSONObject()
            obj.put("titulo", work.titulo)
            obj.put("tipo", work.tipo)
            obj.put("empresa", work.empresa)
            obj.put("tiempo", work.tiempo)
            array.put(obj)
        }
        return array
    }

    private fun chooseFile() {
        val mimeTypes = arrayOf(
            "text/plain"
        )
        var selectFile = Intent(Intent.ACTION_GET_CONTENT)
        selectFile.type = if (mimeTypes.size == 1) mimeTypes[0] else "/"
        if (mimeTypes.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                selectFile.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
        }
        selectFile = Intent.createChooser(selectFile, "Choose a file")
        startActivityForResult(selectFile, Companion.READ_FILE_CODE)

    }

    private fun createProfile(user: User, works: String) {
        val apiInterface = ApiInterface.create().load_data(
            user.name,
            user.email,
            "password",
            user.universidad,
            user.sede,
            user.edad,
            user.sexo.substring(0, 1),
            user.direccion,
            user.telefono_casa,
            user.telefono_celular,
            works
        )

        apiInterface.enqueue(object : Callback<ResponseUser> {  //Pone en cola las solicitudes
            override fun onResponse(
                call: Call<ResponseUser>?,
                response: Response<ResponseUser>?
            ) {

                if (response?.body() != null) {                //Valida si la respuesta del servicio es != null

                    if (response.body()!!.result) {           //Valida que el servicio haya respondido la propiedad result en True
                        val user = response.body()!!.records
                        Toast.makeText(
                            applicationContext,
                            "Si tienes una foto de perfil para el usuario subela.",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        btnCrear.isEnabled = false
                        startActivity(
                            Intent(                         //Iniciar la nueva actividad(Clase Profile, vista activity_profile)
                                applicationContext,
                                Profile::class.java
                            ).putExtra("id", user.id).putExtra("isFromList",true)
                        )
                    } else {
                        Toast.makeText(
                            applicationContext,
                            response.body()!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        btnCrear.isEnabled = true
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Ocurrio un error al registrar usuario",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    btnCrear.isEnabled = true
                }

            }

            override fun onFailure(call: Call<ResponseUser>?, t: Throwable?) {
                btnCrear.isEnabled = true
                Toast.makeText(
                    applicationContext,
                    "Ocurrio un error al conectar con el servidor",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == READ_FILE_CODE) { // Step 1: When a result has been received, check if it is the result for READ_IN_FILE
            if (resultCode == Activity.RESULT_OK) { // Step 2: Check if the operation to retrieve thea ctivity's result is successful
                // Attempt to retrieve the file
                try {
                    data?.data?.let {
                        contentResolver.openInputStream(it)
                    }?.let {
                        val r = BufferedReader(InputStreamReader(it))
                        readFileAndFormat(r)

                    }

                } catch (e: Exception) { // If the app failed to attempt to retrieve the error file, throw an error alert
                    Toast.makeText(
                        this,
                        "Sorry, but there was an error reading in the file",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun readFileAndFormat(r: BufferedReader) {
        var section = 0


        var puestoLaboral = PuestoLaboral()
        var count = 0
        while (true) {
            val line: String? = r.readLine() ?: break

            if (line!!.contains("Datos Personales", true)) {
                btnCrear.isEnabled = true
                section = 1
            } else if (line!!.contains("Histórico Datos Laborales", true)) {
                section = 3
                puestoLaboral = PuestoLaboral()
            } else if (line!!.contains("Datos Laborales", true)) {
                section = 2
                puestoLaboral = PuestoLaboral()

            } else if (line.isNotEmpty()) {
                var key = line.split(": ")[0]
                var value = line.split(": ")[1]

                if (section == 1) {

                    if (key.equals("Nombre", true))
                        user.name = value.replace(".", "")
                    else if (key.equals("Universidad", true))
                        user.universidad = value.replace(".", "")
                    else if (key.equals("Sede", true))
                        user.sede = value.replace(".", "")
                    else if (key.equals("Edad", true))
                        user.edad = Integer.parseInt(value.replace(".", "").replace(" ", ""))
                    else if (key.equals("Sexo", true))
                        user.sexo = value.replace(".", "")
                    else if (key.equals("Dirección", true))
                        user.direccion = value.replace(".", "")
                    else if (key.equals("Teléfono Casa", true))
                        user.telefono_casa = value.replace(".", "")
                    else if (key.equals("Teléfono Celular", true))
                        user.telefono_celular = value.replace(".", "")
                    else if (key.equals("Correo", true))
                        user.email = value
                } else if (section == 2) {


                    if (key.equals("Puesto Actual", true))
                        puestoLaboral.titulo = value.replace(".", "")
                    else if (key.equals("Tiempo Laboral", true))
                        puestoLaboral.tiempo = value.replace(".", "")
                    else if (key.equals("Empresa", true)) {
                        puestoLaboral.empresa = value.replace(".", "")
                        puestoLaboral.tipo = count
                        count++
                        listworks.add(puestoLaboral)
                    }


                } else if (section == 3) {

                    if (key.contains("Puesto Anterior", true))
                        puestoLaboral.titulo = value.replace(".", "")
                    else if (key.contains("Tiempo Laboral", true))
                        puestoLaboral.tiempo = value.replace(".", "")
                    else if (key.contains("Empresa", true)) {
                        puestoLaboral.empresa = value.replace(".", "")
                        puestoLaboral.tipo = count
                        count++
                        listworks.add(puestoLaboral)
                    }

                }

            }

        }

    }

}