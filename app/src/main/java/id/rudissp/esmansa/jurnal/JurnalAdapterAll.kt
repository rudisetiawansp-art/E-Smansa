package id.rudissp.esmansa.jurnal

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.content.edit
import com.google.android.material.card.MaterialCardView

class JurnalAdapterAll(private val data: List<JurnalModelAll>) :
    RecyclerView.Adapter<JurnalAdapterAll.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTanggal: TextView = view.findViewById(R.id.rectanggal)
        val tvMapel: TextView = view.findViewById(R.id.recmapel)
        val tvMateri: TextView = view.findViewById(R.id.recmateri)
        val tvKelas: TextView = view.findViewById(R.id.recKelas)
        val tvJam: TextView = view.findViewById(R.id.recjamke1)
        val tvJam2: TextView = view.findViewById(R.id.recjamke2)
        val tvGuru: TextView = view.findViewById(R.id.recguru)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jurnal, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jurnal = data[position]
        holder.tvTanggal.text = jurnal.tanggal
        holder.tvMapel.text = jurnal.mapel
        holder.tvMateri.text = jurnal.materi
        holder.tvKelas.text = jurnal.kelas
        holder.tvJam.text = jurnal.jamke1
        holder.tvJam2.text = jurnal.jamke2
        holder.tvGuru.text = jurnal.nama
    }
}

