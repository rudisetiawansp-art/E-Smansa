package id.rudissp.esmansa.pantau

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import id.rudissp.esmansa.R

class KelasAdapter(private val listKelas: List<KelasModel>) : RecyclerView.Adapter<KelasAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaKelas: TextView = view.findViewById(R.id.tv_nama_kelas)
        val containerJadwal: LinearLayout = view.findViewById(R.id.container_jadwal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kelas_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataKelas = listKelas[position]
        val context = holder.itemView.context

        holder.tvNamaKelas.text = dataKelas.namaKelas

        // Bersihkan kotak jadwal sebelumnya agar tidak dobel saat didaur ulang
        holder.containerJadwal.removeAllViews()

        // Suntikkan kotak jadwal satu per satu
        for (jadwal in dataKelas.jadwalList) {
            val viewJadwal = LayoutInflater.from(context).inflate(R.layout.item_jadwal_kbm, holder.containerJadwal, false)

            val cardStatus = viewJadwal.findViewById<MaterialCardView>(R.id.card_status)
            val tvGuru = viewJadwal.findViewById<TextView>(R.id.tv_nama_guru)
            val tvMapel = viewJadwal.findViewById<TextView>(R.id.tv_mapel)
            val tvKeterangan = viewJadwal.findViewById<TextView>(R.id.tv_keterangan)
            val tvKelas = viewJadwal.findViewById<TextView>(R.id.tv_kelas)

            tvGuru.text = jadwal.guru
            tvGuru.setTypeface(null, Typeface.BOLD)
            tvMapel.text = jadwal.mapel
            tvKelas.visibility = View.GONE

            when (jadwal.status) {
                "kosong" -> {
                    cardStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_kosong_bg))
                    tvGuru.text = "-"
                    tvMapel.text = ""
                }
                "belum_isi" -> {
                    cardStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_alarm_bg))
                    tvGuru.setTextColor(ContextCompat.getColor(context, R.color.status_alarm_text))
                    tvMapel.setTextColor(ContextCompat.getColor(context, R.color.status_alarm_text))
                    cardStatus.strokeWidth = 4
                    cardStatus.strokeColor = ContextCompat.getColor(context, android.R.color.holo_red_dark)
                }
                "tugas" -> {
                    cardStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_tugas_bg))
                    tvGuru.setTextColor(ContextCompat.getColor(context, R.color.status_tugas_text))
                    tvMapel.setTextColor(ContextCompat.getColor(context, R.color.status_tugas_text))
                    tvKeterangan.visibility = View.VISIBLE
                    tvKeterangan.text = jadwal.keterangan
                    tvKeterangan.setTextColor(ContextCompat.getColor(context, R.color.status_tugas_text))
                }
                "izin" -> {
                    cardStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_izin_bg))
                    tvGuru.setTextColor(ContextCompat.getColor(context, R.color.status_izin_text))
                    tvMapel.setTextColor(ContextCompat.getColor(context, R.color.status_izin_text))
                }
                "hadir" -> {
                    cardStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_hadir_bg))
                    tvGuru.setTextColor(ContextCompat.getColor(context, R.color.status_hadir_text))
                    tvMapel.setTextColor(ContextCompat.getColor(context, R.color.status_hadir_text))
                }
            }

            // Tambahkan ke dalam baris
            holder.containerJadwal.addView(viewJadwal)
        }
    }

    override fun getItemCount(): Int = listKelas.size
}

data class JadwalModel(
    val jamKe: Int,
    val status: String,
    val guru: String,
    val mapel: String,
    val keterangan: String
)

// Model untuk 1 baris kelas (berisi banyak jam pelajaran)
data class KelasModel(
    val namaKelas: String,
    val jadwalList: List<JadwalModel>
)