package id.rudissp.esmansa.lms

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ItemPengumpulanSiswaBinding
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import org.checkerframework.checker.units.qual.Length
import kotlin.time.Duration
import androidx.core.net.toUri

class AdapterPengumpulan(private val context: Context) : RecyclerView.Adapter<AdapterPengumpulan.ViewHolder>() {

    private val listSiswa = mutableListOf<PengumpulanSiswa>()

    fun setData(data: List<PengumpulanSiswa>) {
        listSiswa.clear()
        listSiswa.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPengumpulanSiswaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listSiswa[position])
    }

    override fun getItemCount(): Int = listSiswa.size

    inner class ViewHolder(private val binding: ItemPengumpulanSiswaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(siswa: PengumpulanSiswa) {
            binding.tvNamaSiswa.text = siswa.nama
            binding.tvNisSiswa.text = "NIS: ${siswa.nis}"

            // Menentukan tampilan status berdasarkan isMengumpulkan
            if (siswa.isMengumpulkan) {
                binding.tvStatusKumpul.text = "Sudah"
                ViewCompat.setBackgroundTintList(binding.tvStatusKumpul,
                    ColorStateList.valueOf(ContextCompat.getColor(context,
                        R.color.status_sudah)))
                binding.tvStatusKumpul.setTextColor(Color.WHITE)
                binding.root.setOnClickListener {
                    val namaFile = siswa.fileJawaban
                    if (namaFile.isNotEmpty()) {
                        val urlFile = "https://sman1sukorejo.sch.id/smania/lms/tugasterkumpul/$namaFile"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = urlFile.toUri()

                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Tidak ada aplikasi yang bisa membuka file ini", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Nama file tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                binding.tvStatusKumpul.text = "Belum"
                ViewCompat.setBackgroundTintList(binding.tvStatusKumpul,
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_theme_error)))
                binding.root.setOnClickListener(null)
            }

            // Load Foto (Pastikan URL/Path sesuai dengan letak folder foto di server Smania)
            val urlFoto = "https://sman1sukorejo.sch.id/smania/fotoprofil/" + siswa.foto
            Glide.with(itemView.context)
                .load(urlFoto)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_keluar)
                .into(binding.ivFotoSiswa)
        }
    }
}