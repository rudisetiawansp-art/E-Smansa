package id.rudissp.esmansa.jurtu

// GANTI DUA BARIS DI BAWAH INI SESUAI PACKAGE APLIKASI ANDA:
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.rudissp.esmansa.databinding.ItemRekapJurtuBinding
import id.rudissp.esmansa.databinding.ItemRekapJurtuKegiatanBinding
import androidx.core.graphics.toColorInt
import id.rudissp.esmansa.AppConstants

class RekapAdapter(private val listRekap: ArrayList<RekapPegawaiModel>) :
    RecyclerView.Adapter<RekapAdapter.RekapViewHolder>() {

    inner class RekapViewHolder(val binding: ItemRekapJurtuBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RekapViewHolder {
        val binding =
            ItemRekapJurtuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RekapViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RekapViewHolder, position: Int) {
        val pegawai = listRekap[position]

        // 1. Set data Master Pegawai (Nama & Status)
        holder.binding.tvNamaPegawai.text = pegawai.nama
        holder.binding.tvStatusRekap.text = pegawai.statusText

        // 2. Logika Kondisi Visual (Sudah/Belum Mengisi)
        if (pegawai.daftarJurnal.isEmpty()) {
            // Jika BELUM mengisi: Warna merah, tampilkan "Data Tidak ada"
            holder.binding.tvStatusRekap.backgroundTintList =
                ColorStateList.valueOf("#D32F2F".toColorInt()) // Merah
            holder.binding.tvDataTidakAda.visibility = View.VISIBLE
            holder.binding.llContainerKegiatan.visibility = View.GONE

        } else {
            // Jika SUDAH mengisi: Warna hijau, tampilkan daftar kegiatan
            holder.binding.tvStatusRekap.backgroundTintList =
                ColorStateList.valueOf("#388E3C".toColorInt()) // Hijau
            holder.binding.tvDataTidakAda.visibility = View.GONE
            holder.binding.llContainerKegiatan.visibility = View.VISIBLE

            // PENTING: Bersihkan container sebelum mengisi data baru agar tidak menumpuk saat di-scroll
            holder.binding.llContainerKegiatan.removeAllViews()

            // 3. Looping untuk Memasukkan Daftar Kegiatan ke dalam Kartu
            for (jurnalItem in pegawai.daftarJurnal) {
                // Inflate layout item_kegiatan.xml secara dinamis
                val kegiatanBinding = ItemRekapJurtuKegiatanBinding.inflate(
                    LayoutInflater.from(holder.itemView.context),
                    holder.binding.llContainerKegiatan,
                    false
                )

                // Set teks jam dan deskripsi
                kegiatanBinding.tvJamKegiatan.text = jurnalItem.jam
                kegiatanBinding.tvDeskripsiKegiatan.text = jurnalItem.deskripsi

                // --- LOGIKA MENAMPILKAN FOTO DENGAN GLIDE ---
                if (jurnalItem.foto.isNotEmpty()) {
                    kegiatanBinding.ivFotoKegiatan.visibility = View.VISIBLE

                    // Sesuaikan folder URL dengan letak foto Anda di Dapurhosting
                    // Contoh: jika di dalam folder jurnaltu
                    val imageUrl = AppConstants.BASE_URL + jurnalItem.foto

                    Glide.with(holder.itemView.context)
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(kegiatanBinding.ivFotoKegiatan)

                } else {
                    // Jika tidak ada foto di database, sembunyikan ImageView-nya
                    kegiatanBinding.ivFotoKegiatan.visibility = View.GONE
                }

                // Masukkan layout kegiatan yang sudah diisi ke dalam container CardView
                holder.binding.llContainerKegiatan.addView(kegiatanBinding.root)
            }
        }
    }

    override fun getItemCount(): Int {
        return listRekap.size
    }
}

data class JurnalItemModel(
    val idJurnal: String,
    val jam: String,
    val deskripsi: String,
    val foto: String
)

// Data class utama untuk menampung identitas pegawai dan daftar kegiatannya (kartu utama)
data class RekapPegawaiModel(
    val nama: String,
    val nip: String,
    val daftarJurnal: List<JurnalItemModel>
) {
    // Variabel pintar untuk menentukan status teks berdasarkan isi daftarJurnal
    val statusText: String
        get() = if (daftarJurnal.isEmpty()) "Belum Mengisi" else "Sudah Mengisi"
}