package com.example.appprofiles.Interfaces

import com.example.appprofiles.Models.ResponseUser
import com.example.appprofiles.Models.ResponseUsers
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


interface ApiInterface {

    @FormUrlEncoded
    @POST("api/load_data")
    fun load_data(@Field ("name") name:String, @Field ("email") email:String,
              @Field ("password") password:String,
              @Field ("universidad") universidad:String,
              @Field ("sede") sede:String,
              @Field ("edad") edad:Int,
              @Field ("sexo") sexo:String,
              @Field ("direccion") direccion:String,
              @Field ("telefono_casa") telefono_casa:String,
              @Field ("telefono_celular") telefono_celular:String,
              @Field("works") works:String
    ) : Call<ResponseUser>

    @FormUrlEncoded
    @POST("api/edit_user")
    fun edit_user(@Field ("name") name:String,
                  @Field ("email") email:String,
                  @Field ("password") password:String,
                  @Field ("universidad") universidad:String,
                  @Field ("sede") sede:String,
                  @Field ("edad") edad:Int,
                  @Field ("sexo") sexo:String,
                  @Field ("direccion") direccion:String,
                  @Field ("telefono_casa") telefono_casa:String,
                  @Field ("telefono_celular") telefono_celular:String,
                  @Field ("id") id:Int
    ) : Call<ResponseUser>

    @Multipart
    @POST("api/load_image")
    fun upload_photo(@Part ("id") id:Int,
                     @Part ("name") name:String,
                  @Part imagen:MultipartBody.Part): Call<ResponseUser>

    @FormUrlEncoded
    @POST("api/login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ) : Call<ResponseUser>

    @GET("api/listar_usuarios")
    fun listar_usuarios(): Call<ResponseUsers>

    @GET("api/consultar_usuario")
    fun consultar_usuario(@Query("id")id: Int): Call<ResponseUser>

    companion object {

        var BASE_URL = "http://192.168.1.66:8000/"

        fun create() : ApiInterface {

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()


            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(client)
                .build()
            return retrofit.create(ApiInterface::class.java)

        }
    }
}