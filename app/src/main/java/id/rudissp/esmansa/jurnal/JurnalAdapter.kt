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

class JurnalAdapter(
    private val list: MutableList<JurnalModel>,
    private val context: Context) :
    RecyclerView.Adapter<JurnalAdapter.ViewHolder>() {
    private val prefs: SharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recKelas: TextView = itemView.findViewById(R.id.recKelas)
        val recjamke1: TextView = itemView.findViewById(R.id.recjamke1)
        val recjamke2: TextView = itemView.findViewById(R.id.recjamke2)
        val rectanggal: TextView = itemView.findViewById(R.id.rectanggal)
        val recguru: TextView = itemView.findViewById(R.id.recguru)
        val recmapel: TextView = itemView.findViewById(R.id.recmapel)
        val recmateri: TextView = itemView.findViewById(R.id.recmateri)
        var more: ImageView = itemView.findViewById(R.id.more)
        var recCard: MaterialCardView = itemView.findViewById(R.id.recCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_jurnal, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jurnal = list[position]
        holder.recKelas.text = jurnal.kelas
        holder.recjamke1.text = jurnal.jamke1
        holder.recjamke2.text = jurnal.jamke2
        holder.rectanggal.text = formatTanggalIndo(jurnal.tanggal)
        holder.recguru.text = jurnal.nama
        holder.recmapel.text = jurnal.mapel
        holder.recmateri.text = jurnal.materi

        if (holder.recmateri.text.toString() == "TUGAS") {
            val color = ContextCompat.getColor(context, R.color.md_theme_primaryContainer);
            holder.recCard.setCardBackgroundColor(color)
        } else{
            val color = ContextCompat.getColor(context, R.color.md_theme_surface);
            holder.recCard.setCardBackgroundColor(color)
        }


        // Menangani klik pada tombol Edit
        holder.recCard.setOnClickListener {
            val intent = Intent(context, TambahJurnal::class.java)
            intent.putExtra("id", jurnal.id)
            intent.putExtra("username", jurnal.username)
            intent.putExtra("nama", jurnal.nama)
            intent.putExtra("mapel", jurnal.mapel)
            intent.putExtra("tanggal", jurnal.tanggal)
            intent.putExtra("kelas", jurnal.kelas)
            intent.putExtra("jamke1", jurnal.jamke1)
            intent.putExtra("jamke2", jurnal.jamke2)
            intent.putExtra("materi", jurnal.materi)
            intent.putExtra("kehadiran", jurnal.kehadiran)
            intent.putExtra("keterangan", jurnal.keterangan)
            intent.putExtra("mode", "editmode")
            context.startActivity(intent)
        }

        // Menangani klik pada tombol Hapus
        holder.more.setOnClickListener {
            val popup = PopupMenu(
                context, holder.more
            )
            popup.inflate(R.menu.popup)
            popup.setOnMenuItemClickListener { item: MenuItem ->
                val itemID = item.itemId
                if (itemID == R.id.menunya_hapus) {
                    val aaaa = MaterialAlertDialogBuilder(context)
                    aaaa.setMessage(
                        "Yakin akan menghapus data ini?"
                    )
                    aaaa.setPositiveButton("Ya, Hapus...") { _: DialogInterface?, _: Int ->
                        deleteJurnal(jurnal.id, position)
                        Toast.makeText(context, "Data Jurnal  dihapus!!", Toast.LENGTH_SHORT).show()
                    }
                    aaaa.show()
                }
                true
            }
            popup.show()


        }
    }

    private fun deleteJurnal(id: String, position: Int) {
        val url = AppConstants.HAPUS_JURNAL + "?id=$id"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val status = response.getString("status")
                if (status == "success") {
                    // Hapus item dari list dan update RecyclerView
                    list.removeAt(position)
                    notifyItemRemoved(position)
                }
            },
            { error ->
                Toast.makeText(context, "Gagal menghapus jurnal", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(context).add(request)
    }

    private fun formatTanggalIndo(tanggal: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(tanggal)
            val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
            outputFormat.format(date!!)
        } catch (e: Exception) {
            tanggal // fallback jika parsing gagal
        }
    }

    fun updateData(newList: List<JurnalModel>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
data class JurnalModel(
    val id: String,
    val username: String,
    val nama: String,
    val mapel: String,
    val tanggal: String,
    val kelas: String,
    val jamke1: String,
    val jamke2: String,
    val materi: String,
    val kehadiran: String,
    val keterangan: String
)

