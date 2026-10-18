package id.rudissp.esmansa.lms

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.rudissp.esmansa.R
import androidx.core.graphics.toColorInt
import kotlin.jvm.java

// Model Data
data class LmsModel(
    val idTugas: String,
    val jenis: String,
    val mapel: String,
    val judul: String,
    val daftarKelas: String,
    val deadline: String
)

// Adapter
class LmsAdapter(private val context: Context, private val listLms: List<LmsModel>) : RecyclerView.Adapter<LmsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJenisBadge: TextView = view.findViewById(R.id.tv_jenis_badge)
        val tvMapel: TextView = view.findViewById(R.id.tv_mapel)
        val tvJudul: TextView = view.findViewById(R.id.tv_judul)
        val tvKelas: TextView = view.findViewById(R.id.tv_kelas)
        val tvDeadline: TextView = view.findViewById(R.id.tv_deadline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lms, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLms[position]

        holder.tvMapel.text = item.mapel
        holder.tvJudul.text = item.judul
        holder.tvKelas.text = item.daftarKelas

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailLMS::class.java)
            intent.putExtra("ID_TUGAS", item.idTugas)
            // Bisa juga mengirim data lain jika tidak ingin query ulang dari awal
            intent.putExtra("JUDUL", item.judul)
            intent.putExtra("IS_ONLINE", item.jenis) // untuk ngecek perlu tab pengumpulan atau tidak
            context.startActivity(intent)
        }

        // Logika Pembedaan Warna & Teks Berdasarkan Jenis
        if (item.jenis.equals("materi", ignoreCase = true)) {
            holder.tvJenisBadge.text = "MATERI"
            holder.tvJenisBadge.backgroundTintList = ColorStateList.valueOf("#00897B".toColorInt())

            // Jika materi, biasanya tidak butuh text deadline merah, bisa disembunyikan atau diubah teksnya
            if (item.deadline == "null" || item.deadline.isEmpty()) {
                holder.tvDeadline.visibility = View.GONE
            } else {
                holder.tvDeadline.visibility = View.VISIBLE
                holder.tvDeadline.text = "Dibuat: ${item.deadline}"
                holder.tvDeadline.setTextColor("#757575".toColorInt()) // Warna abu-abu normal
            }
        } else {
            holder.tvJenisBadge.text = "PENUGASAN"
            // Set warna background badge menjadi Merah/Oranye untuk Penugasan
            holder.tvJenisBadge.backgroundTintList = ColorStateList.valueOf("#E53935".toColorInt())

            holder.tvDeadline.visibility = View.VISIBLE
            holder.tvDeadline.text = "Tenggat: ${item.deadline}"
            holder.tvDeadline.setTextColor("#D32F2F".toColorInt())
        }
    }

    override fun getItemCount(): Int = listLms.size
}