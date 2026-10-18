package id.rudissp.esmansa.presensi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import id.rudissp.esmansa.R

class RekapHarianAdapter(private val items: List<RekapHarianModel>) :
    RecyclerView.Adapter<RekapHarianAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nama: TextView = view.findViewById(R.id.tvrh_nama)
        val masuk: TextView = view.findViewById(R.id.tvrh_Masuk)
        val pulang: TextView = view.findViewById(R.id.tvrh_Pulang)
        val jenis: TextView = view.findViewById(R.id.tvrh_jenis)
        val kurang: TextView = view.findViewById(R.id.tvrh_kwk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rekap_harian, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nama.text = item.tanggal
        holder.masuk.text = item.masuk
        holder.pulang.text = item.pulang
        holder.jenis.text = item.jenis
        holder.kurang.text = item.kurang

        if (item.shift == "malam") {
            holder.jenis.text = "Piket"

            val color = ContextCompat.getColor(holder.itemView.context, R.color.md_theme_tertiary_highContrast)
            holder.jenis.setTextColor(color)
            holder.masuk.setTextColor(color)
            holder.pulang.setTextColor(color)

        } else{
            holder.jenis.text = item.jenis
        }
    }

    override fun getItemCount(): Int = items.size
}

data class RekapHarianModel(
    val tanggal: String, // bisa ditampilkan sebagai tanggal atau nama user tergantung konteks
    val masuk: String,
    val pulang: String,
    val jenis: String,
    val shift: String,
    val kurang: String
)

