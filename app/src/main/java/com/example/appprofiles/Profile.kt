package com.example.appprofiles

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ProgressDialog.show
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appprofiles.Interfaces.ApiInterface
import com.example.appprofiles.Interfaces.ApiInterface.Companion.BASE_URL
import com.example.appprofiles.Models.ResponseUser
import com.example.appprofiles.adapters.AdapterWorks
import com.example.appprofiles.utils.Utils
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody


class Profile : AppCompatActivity() {

    companion object {
        const val PERMISSION_CODE = 1
        const val REQUEST_FILE = 2
    }

    var file:File? = null
    var uri:Uri? = null

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

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.getItemId()) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        if(intent.extras!!.containsKey("isFromList")){
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
        } else{
            findViewById<Button>(R.id.btn_logout).visibility = View.VISIBLE
            findViewById<Button>(R.id.btn_logout).setOnClickListener {
                startActivity(
                    Intent(                         //Iniciar la nueva actividad(Clase Profile, vista activity_profile)
                        this,
                        MainActivity::class.java
                    )
                )
                finish()
            }
        }
        loadData(intent.extras!!.getInt("id", 13))
        Utils.checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_CODE, this)
        Utils.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_CODE, this)
        findViewById<Button>(R.id.btn_update).setOnClickListener {
            updateProfile()
        }

        findViewById<ImageView>(R.id.imgProfile).setOnClickListener {
            val pickPhoto = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(pickPhoto, REQUEST_FILE) //one can be replaced with any action code

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_FILE ->
                if (resultCode == Activity.RESULT_OK) {
                    if (resultCode === RESULT_OK) {
                        val selectedImage: Uri = data!!.getData()!!
                        file = File(getPath(selectedImage))
                        this.uri = selectedImage
                        findViewById<ImageView>(R.id.imgProfile).setImageURI(selectedImage)
                    }
                }
        }
    }
    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null) ?: return null
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s = cursor.getString(column_index)
        cursor.close()
        return s
    }

    private fun updateProfile() {
        if(file!=null){
            uploadImage()
        }
        val apiInterface = ApiInterface.create().edit_user(
            findViewById<EditText>(R.id.et_name).text.toString(),
            findViewById<EditText>(R.id.et_correo).text.toString(),
            findViewById<EditText>(R.id.et_contrasena).text.toString(),
            findViewById<EditText>(R.id.et_universidad).text.toString(),
            findViewById<EditText>(R.id.et_sede).text.toString(),
            Integer.parseInt(findViewById<EditText>(R.id.et_edad).text.toString()),
            findViewById<EditText>(R.id.et_sexo).text.toString().substring(0, 1),
            findViewById<EditText>(R.id.et_direccion).text.toString(),
            findViewById<EditText>(R.id.et_telefonocasa).text.toString(),
            findViewById<EditText>(R.id.et_telefonocelular).text.toString(),
            intent.extras!!.getInt("id")
        )

        apiInterface.enqueue(object : Callback<ResponseUser> {  //Pone en cola las solicitudes
            override fun onResponse(
                call: Call<ResponseUser>?,
                response: Response<ResponseUser>?
            ) {

                if (response?.body() != null) {                //Valida si la respuesta del servicio es != null

                    if (response.body()!!.result) {
                        Toast.makeText(applicationContext, "Actualizado correctamente", Toast.LENGTH_SHORT)
                            .show()
                        val user = response.body()!!.records
                        findViewById<EditText>(R.id.et_name).setText(user.name)
                        findViewById<EditText>(R.id.et_universidad).setText(user.universidad)
                        findViewById<EditText>(R.id.et_sede).setText(user.sede)
                        findViewById<EditText>(R.id.et_edad).setText(user.edad.toString())
                        findViewById<EditText>(R.id.et_sexo).setText(user.sexo)
                        findViewById<EditText>(R.id.et_direccion).setText(user.direccion)
                        findViewById<EditText>(R.id.et_telefonocasa).setText(user.telefono_casa)
                        findViewById<EditText>(R.id.et_telefonocelular).setText(user.telefono_celular)
                        findViewById<EditText>(R.id.et_correo).setText(user.email)
                        loadImage(user.imagen)
                    } else
                        Toast.makeText(
                            applicationContext,
                            response.body()!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                } else
                    Toast.makeText(applicationContext, "Usuario no encontrado", Toast.LENGTH_SHORT)
                        .show()

            }

            override fun onFailure(call: Call<ResponseUser>?, t: Throwable?) {

                Toast.makeText(
                    applicationContext,
                    "Ocurrio un error al conectar con el servidor",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }

    private fun loadImage(imagen: String) {

        val imgProfile: ImageView = findViewById(R.id.imgProfile)
        if(imagen !=null && imagen.isNotEmpty())
            Glide.with(imgProfile).load(BASE_URL + "profiles/" + imagen)
                .into(imgProfile)
        else
            Glide.with(imgProfile).load("https://image.flaticon.com/icons/png/512/64/64572.png")
                .into(imgProfile)

    }

    private fun uploadImage() {
        // calling from global scope
        GlobalScope.launch {
            val compressedImageFile = Compressor.compress(applicationContext, file!!) {
                resolution(640, 640)
                quality(10)
                format(Bitmap.CompressFormat.JPEG)
            }


            val requestFile = compressedImageFile!!
                .asRequestBody(contentResolver.getType(uri!!)!!.toMediaTypeOrNull())

            val body = MultipartBody.Part.createFormData(
                "imagen",
                compressedImageFile!!.name,
                requestFile
            )

            val apiInterface = ApiInterface.create().upload_photo(
                intent.extras!!.getInt("id"),
                findViewById<EditText>(R.id.et_name).text.toString(),
                body
            )

            apiInterface.enqueue(object : Callback<ResponseUser> {  //Pone en cola las solicitudes
                override fun onResponse(
                    call: Call<ResponseUser>?,
                    response: Response<ResponseUser>?
                ) {

                    if (response?.body() != null) {                //Valida si la respuesta del servicio es != null

                        if (response.body()!!.result) {           //Valida que el servicio haya respondido la propiedad result en True
                            val user = response.body()!!.records
                            Toast.makeText(applicationContext, "Imagen Actualizada", Toast.LENGTH_SHORT)
                                .show()
                            findViewById<EditText>(R.id.et_name).setText(user.name)
                            findViewById<EditText>(R.id.et_universidad).setText(user.universidad)
                            findViewById<EditText>(R.id.et_sede).setText(user.sede)
                            findViewById<EditText>(R.id.et_edad).setText(user.edad.toString())
                            findViewById<EditText>(R.id.et_sexo).setText(user.sexo)
                            findViewById<EditText>(R.id.et_direccion).setText(user.direccion)
                            findViewById<EditText>(R.id.et_telefonocasa).setText(user.telefono_casa)
                            findViewById<EditText>(R.id.et_telefonocelular).setText(user.telefono_celular)
                            findViewById<EditText>(R.id.et_correo).setText(user.email)
                            var list: RecyclerView = findViewById(R.id.rv_list)
                            list.setHasFixedSize(true)
                            list.layoutManager = LinearLayoutManager(this@Profile)
                            list.adapter = AdapterWorks(user.works)
                            loadImage(user.imagen)
                        } else
                            Toast.makeText(
                                applicationContext,
                                response.body()!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                    } else
                        Toast.makeText(
                            applicationContext,
                            "Usuario no encontrado",
                            Toast.LENGTH_SHORT
                        )
                            .show()

                }

                override fun onFailure(call: Call<ResponseUser>?, t: Throwable?) {

                    Toast.makeText(
                        applicationContext,
                        "Ocurrio un error al conectar con el servidor",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
        }
    }

    private fun loadData(int: Int) {
        val apiInterface = ApiInterface.create().consultar_usuario(int)


        apiInterface.enqueue(object : Callback<ResponseUser> {  //Pone en cola las solicitudes
            override fun onResponse(
                call: Call<ResponseUser>?,
                response: Response<ResponseUser>?
            ) {

                if (response?.body() != null) {                //Valida si la respuesta del servicio es != null

                    if (response.body()!!.result) {           //Valida que el servicio haya respondido la propiedad result en True
                        val user = response.body()!!.records
                        findViewById<EditText>(R.id.et_name).setText(user.name)
                        findViewById<EditText>(R.id.et_universidad).setText(user.universidad)
                        findViewById<EditText>(R.id.et_sede).setText(user.sede)
                        findViewById<EditText>(R.id.et_edad).setText(user.edad.toString())
                        findViewById<EditText>(R.id.et_sexo).setText(user.sexo)
                        findViewById<EditText>(R.id.et_direccion).setText(user.direccion)
                        findViewById<EditText>(R.id.et_telefonocasa).setText(user.telefono_casa)
                        findViewById<EditText>(R.id.et_telefonocelular).setText(user.telefono_celular)
                        findViewById<EditText>(R.id.et_correo).setText(user.email)
                        var list: RecyclerView = findViewById(R.id.rv_list)
                        list.setHasFixedSize(true)
                        list.layoutManager = LinearLayoutManager(this@Profile)
                        list.adapter = AdapterWorks(user.works)
                        loadImage(user.imagen)
                    } else
                        Toast.makeText(
                            applicationContext,
                            response.body()!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                } else
                    Toast.makeText(applicationContext, "Usuario no encontrado", Toast.LENGTH_SHORT)
                        .show()

            }

            override fun onFailure(call: Call<ResponseUser>?, t: Throwable?) {

                Toast.makeText(
                    applicationContext,
                    "Ocurrio un error al conectar con el servidor",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }


}