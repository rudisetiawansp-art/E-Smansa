package id.rudissp.esmansa.presensi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.rudissp.esmansa.R

class HBAdapter(
    private val list: List<HariLibur>,
    private val onHapusClick: (HariLibur) -> Unit
) : RecyclerView.Adapter<HBAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTanggal: TextView = view.findViewById(R.id.item_hb_tgl)
        val btnHapus: ImageView = view.findViewById(R.id.item_hb_del)
        val tvKeterangan: TextView = view.findViewById(R.id.item_hb_ket)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hb, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val libur = list[position]
        holder.tvTanggal.text = libur.tanggal
        holder.tvKeterangan.text = libur.keterangan

        holder.btnHapus.setOnClickListener {
            onHapusClick(libur)
        }
    }
}
