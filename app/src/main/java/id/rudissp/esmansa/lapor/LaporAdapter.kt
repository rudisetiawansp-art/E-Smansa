package id.rudissp.esmansa.lapor

import android.content.Context
import android.graphics.Color
import android.system.Os.link
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ItemLaporBinding

class LaporAdapter(
    private val context: Context,
    private val list: MutableList<LaporanModel>
) : RecyclerView.Adapter<LaporAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLaporBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLaporBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val laporan = list[position]
        with(receiver = holder.binding) {

            // Set Teks Dasar
            tvNamaPelapor.text = laporan.namaPelapor
            tvTglLapor.text = laporan.tglLapor
            tvKategori.text = laporan.kategori
            tvJudul.text = laporan.judul
            tvIsiLaporan.text = laporan.isiLaporan

            if (laporan.namaPelapor.isNotEmpty()) {
                tvAvatar.text = laporan.namaPelapor.substring(0, 1).uppercase()
            }

            // --- LOGIKA WARNA STATUS ---
            tvStatus.text = laporan.statusLaporan
            when (laporan.statusLaporan.lowercase()) {
                "menunggu" -> tvStatus.setTextColor("#E65100".toColorInt())
                "diproses" -> tvStatus.setTextColor("#1565C0".toColorInt())
                "selesai" -> tvStatus.setTextColor("#2E7D32".toColorInt())
            }

            if (laporan.privasi == "Private") {
                tvPrivasi.text = "Privat"
                tvPrivasi.setTextColor("#D32F2F".toColorInt())
                ivIconPrivasi.setColorFilter("#D32F2F".toColorInt())
                layoutGuruTujuan.visibility = View.VISIBLE
            } else {
                tvPrivasi.text = "Publik"
                tvPrivasi.setTextColor("#757575".toColorInt())
                ivIconPrivasi.setImageResource(R.drawable.ic_public)
                ivIconPrivasi.setColorFilter("#757575".toColorInt())
                layoutGuruTujuan.visibility = View.GONE
            }

            // --- LOGIKA GAMBAR DENGAN GLIDE ---
            if (laporan.fotoUrl.isNotEmpty() && laporan.fotoUrl != "null") {
                imgBukti.visibility = View.VISIBLE
                Log.d("FOTO_URL",laporan.fotoUrl)
                Glide.with(root.context)
                    .load(laporan.fotoUrl)
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(24)))
                    .into(imgBukti)
            } else {
                imgBukti.visibility = View.GONE
            }
        }

    }
}

data class LaporanModel(
    val idLaporan: String,
    val kategori: String,
    val judul: String,
    val isiLaporan: String,
    val privasi: String,
    val statusLaporan: String,
    val tglLapor: String,
    val namaPelapor: String,
    val fotoUrl: String
)