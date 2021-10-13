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
import com.example.appprofiles.Models.PuestoLaboral
import com.example.appprofiles.Models.User
import com.example.appprofiles.utils.Utils
import java.io.BufferedReader
import java.io.InputStreamReader

class UsrList : AppCompatActivity() {
    companion object{
        const val PERMISSION_CODE=1
        const val READ_FILE_CODE=2
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usr_list)
        Utils.checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_CODE,this)
        findViewById<Button>(R.id.btn_file).setOnClickListener {
        chooseFile()
        }
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
        var section=0
        val user= User()
        var listworks=ArrayList<PuestoLaboral>()
        var puestoLaboral=PuestoLaboral()
        var count=0
        while (true) {
            val line: String? = r.readLine() ?: break

            if(line!!.contains("Datos Personales",true)) {
            section=1
            }

            else if(line!!.contains("Histórico Datos Laborales",true)){
                section=3
                puestoLaboral= PuestoLaboral()
            }

            else if (line!!.contains("Datos Laborales",true)){
            section=2
                puestoLaboral=PuestoLaboral()

            }

            else if(line.isNotEmpty()){
                var key=line.split(":")[0]
                var value=line.split(":")[1]

                if (section==1){

                    if(key.equals("Nombre",true))
                        user.name=value.replace(".","")
                    else if (key.equals("Universidad",true))
                        user.universidad=value.replace(".","")
                    else if (key.equals("Sede",true))
                        user.sede=value.replace(".","")
                    else if (key.equals("Edad",true))
                        user.edad=Integer.parseInt(value.replace(".","").replace(" ",""))
                    else if (key.equals("Sexo",true))
                        user.sexo=value.replace(".","")
                    else if (key.equals("Dirección",true))
                        user.direccion=value.replace(".","")
                    else if (key.equals("Teléfono Casa",true))
                        user.telefono_casa=value.replace(".","")
                    else if (key.equals("Teléfono Celular",true))
                        user.telefono_celular=value.replace(".","")
                    else if (key.equals("Correo",true))
                        user.email=value
                }

                else if (section==2){


                    if(key.equals("Puesto Actual",true))
                        puestoLaboral.titulo=value.replace(".","")
                    else if (key.equals("Tiempo Laboral",true))
                        puestoLaboral.tiempo=value.replace(".","")
                    else if (key.equals("Empresa",true)){
                        puestoLaboral.empresa=value.replace(".","")
                        puestoLaboral.tipo=count
                        count++
                        listworks.add(puestoLaboral)
                    }


                }

                else if (section==3){

                    if(key.contains("Puesto Anterior",true))
                        puestoLaboral.titulo=value.replace(".","")
                    else if (key.contains("Tiempo Laboral",true))
                        puestoLaboral.tiempo=value.replace(".","")
                    else if (key.contains("Empresa",true)){
                        puestoLaboral.empresa=value.replace(".","")
                        puestoLaboral.tipo=count
                        count++
                        listworks.add(puestoLaboral)
                    }

                }

            }

        }

        print(user)
    }

}