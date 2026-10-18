package id.rudissp.esmansa.jurnal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.rudissp.esmansa.R

class JurnalAdapterGabungan(private val jurnalItems: List<JurnalItemKelas>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_DETAIL = 1

    override fun getItemViewType(position: Int): Int {
        return when (jurnalItems[position]) {
            is JurnalItemKelas.Header -> TYPE_HEADER
            is JurnalItemKelas.Detail -> TYPE_DETAIL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_jurnal_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_jurnal_detail, parent, false)
            DetailViewHolder(view)
        }
    }

    override fun getItemCount(): Int = jurnalItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = jurnalItems[position]) {
            is JurnalItemKelas.Header -> (holder as HeaderViewHolder).bind(item)
            is JurnalItemKelas.Detail -> (holder as DetailViewHolder).bind(item)
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggalHeader)
        fun bind(item: JurnalItemKelas.Header) {
            tvTanggal.text = item.tanggal
        }
    }

    inner class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJamke: TextView = itemView.findViewById(R.id.recjamke)
        private val tvGuru: TextView = itemView.findViewById(R.id.recguru)
        private val tvMapel: TextView = itemView.findViewById(R.id.recmapel)
        private val tvMateri: TextView = itemView.findViewById(R.id.recmateri)

        fun bind(item: JurnalItemKelas.Detail) {
            tvJamke.text = "Jamke\n${item.jamke1} - ${item.jamke2}"
            tvGuru.text = item.nama
            tvMapel.text = item.mapel
            tvMateri.text = item.materi
        }
    }
}

sealed class JurnalItemKelas {
    data class Header(val tanggal: String) : JurnalItemKelas()
    data class Detail(
        val jamke1: String,
        val jamke2: String,
        val nama: String,
        val mapel: String,
        val materi: String
    ) : JurnalItemKelas()
}
