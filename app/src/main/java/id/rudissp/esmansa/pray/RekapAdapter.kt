package id.rudissp.esmansa.pray

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.rudissp.esmansa.databinding.ItemPrayRekapBinding

class RekapAdapter(private val siswaList: List<SiswaModel>) :
    RecyclerView.Adapter<RekapAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPrayRekapBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPrayRekapBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val siswa = siswaList[position]
        with(holder.binding) {
            tvNama.text = siswa.nama
            tvWaktu.text = siswa.waktu
            tvKeterangan.text = siswa.keterangan

            // Logic Warna Berdasarkan Keterangan
            when (siswa.keterangan) {
                "Hadir" -> {
                    tvKeterangan.setTextColor(Color.parseColor("#2E7D32")) // Hijau
                }
                "Halangan" -> {
                    tvKeterangan.setTextColor(Color.parseColor("#FB8C00")) // Orange
                }
                else -> {
                    tvKeterangan.setTextColor(Color.RED) // Belum
                }
            }
        }
    }

    override fun getItemCount(): Int = siswaList.size
}

data class SiswaModel(
    val nama: String,
    val waktu: String,
    val keterangan: String,
)