package id.rudissp.esmansa.lms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.rudissp.esmansa.R
import java.text.SimpleDateFormat
import java.util.Locale

// Data Class untuk menampung JSON Diskusi
data class DiskusiLMS(
    val idDiskusi: Int,
    val pengirimTipe: String, // 'guru' atau 'siswa'
    val pengirimNama: String,
    val pesan: String,
    val waktu: String
)

class AdapterDiskusi : RecyclerView.Adapter<AdapterDiskusi.ViewHolder>() {

    private val listDiskusi = mutableListOf<DiskusiLMS>()

    fun setData(data: List<DiskusiLMS>) {
        listDiskusi.clear()
        listDiskusi.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diskusi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listDiskusi[position])
    }

    override fun getItemCount(): Int = listDiskusi.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Deklarasi View Kiri
        private val layoutKiri: LinearLayout = itemView.findViewById(R.id.layout_chat_kiri)
        private val tvNamaKiri: TextView = itemView.findViewById(R.id.tv_nama_kiri)
        private val tvPesanKiri: TextView = itemView.findViewById(R.id.tv_pesan_kiri)
        private val tvWaktuKiri: TextView = itemView.findViewById(R.id.tv_waktu_kiri)

        private val layoutKanan: LinearLayout = itemView.findViewById(R.id.layout_chat_kanan)
        private val tvPesanKanan: TextView = itemView.findViewById(R.id.tv_pesan_kanan)
        private val tvWaktuKanan: TextView = itemView.findViewById(R.id.tv_waktu_kanan)

        fun bind(diskusi: DiskusiLMS) {
            var waktuJam = diskusi.waktu
            try {
                val sdfInput = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val sdfOutput = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = sdfInput.parse(diskusi.waktu)
                if (date != null) waktuJam = sdfOutput.format(date)
            } catch (e: Exception) { e.printStackTrace() }

            if (diskusi.pengirimTipe == "guru") {

                layoutKanan.visibility = View.VISIBLE
                layoutKiri.visibility = View.GONE

                tvPesanKanan.text = diskusi.pesan
                tvWaktuKanan.text = waktuJam
            } else {
                layoutKiri.visibility = View.VISIBLE
                layoutKanan.visibility = View.GONE

                tvNamaKiri.text = diskusi.pengirimNama
                tvPesanKiri.text = diskusi.pesan
                tvWaktuKiri.text = waktuJam
            }
        }
    }
}