package id.rudissp.esmansa.presensi.siswa

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.rudissp.esmansa.R

class PresensiSiswaHarianAdapter(private val list: MutableList<PresensiSiswaHarian>) :
    RecyclerView.Adapter<PresensiSiswaHarianAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_presensi_siswa_harian, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PresensiSiswaIndividu::class.java)
            intent.putExtra("nis", list[position].nis)
            intent.putExtra("nama", list[position].nama)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<PresensiSiswaHarian>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNama = itemView.findViewById<TextView>(R.id.tvNama)
        private val tvMasuk = itemView.findViewById<TextView>(R.id.tvMasuk)
        private val tvPulang = itemView.findViewById<TextView>(R.id.tvPulang)
        private val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)

        fun bind(data: PresensiSiswaHarian) {
            tvNama.text = data.nama
            tvMasuk.text = data.masuk
            tvPulang.text = data.pulang
            tvStatus.text = data.status
        }

    }
}