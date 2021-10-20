package com.example.appprofiles.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appprofiles.Interfaces.ApiInterface.Companion.BASE_URL
import com.example.appprofiles.Models.User
import com.example.appprofiles.Profile
import com.example.appprofiles.R

class AdapterPerfiles(val listUsers:List<User>,val context: Context): RecyclerView.Adapter<AdapterPerfiles.ViewWorks>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewWorks {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_profile, parent, false)

        return ViewWorks(view)
    }

    override fun onBindViewHolder(holder: ViewWorks, position: Int) {
        holder.viewL.setOnClickListener {
            context.startActivity(
                Intent(                         //Iniciar la nueva actividad(Clase Profile, vista activity_profile)
                    context,
                    Profile::class.java
                ).putExtra("id", listUsers[position].id).putExtra("isFromList",true)
            )
        }
        holder.empresa.text="Nombre: "+listUsers[position].name
        holder.puesto.text="Puesto: "+listUsers[position].titulo
        holder.tiempo.text="Tiempo: "+listUsers[position].tiempo
        if(listUsers[position].imagen !=null && listUsers[position].imagen.isNotEmpty())
            Glide.with(holder.img).load(BASE_URL + "profiles/" + listUsers[position].imagen)
                .into(holder.img)
        else
            Glide.with(holder.img).load("https://image.flaticon.com/icons/png/512/64/64572.png")
                .into(holder.img)
    }

    override fun getItemCount(): Int {
        return listUsers.size
    }

    class ViewWorks(view: View): RecyclerView.ViewHolder(view) {
        lateinit var puesto:TextView
        lateinit var empresa:TextView
        lateinit var tiempo:TextView
        lateinit var img:ImageView
        lateinit var viewL: LinearLayout

        init {
            puesto = view.findViewById(R.id.puesto)
            empresa = view.findViewById(R.id.empresa)
            tiempo = view.findViewById(R.id.tiempo)
            img = view.findViewById(R.id.img)
            viewL = view.findViewById(R.id.view)
        }

    }
}