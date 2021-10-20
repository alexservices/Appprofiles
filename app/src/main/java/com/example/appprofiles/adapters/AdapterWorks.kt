package com.example.appprofiles.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appprofiles.Models.PuestoLaboral
import com.example.appprofiles.R

class AdapterWorks(val listWorks:List<PuestoLaboral>): RecyclerView.Adapter<AdapterWorks.ViewWorks>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewWorks {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_work, parent, false)

        return ViewWorks(view)
    }

    override fun onBindViewHolder(holder: ViewWorks, position: Int) {
        holder.empresa.text="Empresa: "+listWorks[position].empresa
        holder.puesto.text="Puesto: "+listWorks[position].titulo
        holder.tiempo.text="Tiempo: "+listWorks[position].tiempo
        if(listWorks[position].tipo==0)
            holder.historico.text="Puesto actual"
        else
            holder.historico.text = "Historico:  "+listWorks[position].tipo
    }

    override fun getItemCount(): Int {
        return listWorks.size
    }

    class ViewWorks(view: View): RecyclerView.ViewHolder(view) {
        lateinit var puesto:TextView
        lateinit var empresa:TextView
        lateinit var tiempo:TextView
        lateinit var historico:TextView

        init {
            puesto = view.findViewById(R.id.puesto)
            empresa = view.findViewById(R.id.empresa)
            tiempo = view.findViewById(R.id.tiempo)
            historico = view.findViewById(R.id.historico)
        }

    }
}