package id.rudissp.esmansa.sppd

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ItemSppdBinding
import org.json.JSONObject

data class SppdModel(
    val id: String,
    val username: String,
    val nama: String,
    val jabatan: String,
    val nip: String,
    val tanggal: String,
    val lokasi: String,
    val deskripsi: String,
    val foto: String,
    val foto2:String
)


class SppdAdapter(
    private val context: Context,
    private val list: MutableList<SppdModel>
) : RecyclerView.Adapter<SppdAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSppdBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSppdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        with(holder.binding) {
            tvNama.text = data.nama
            tvTanggal.text = data.tanggal
            tvLokasi.text = data.lokasi
            tvDeskripsi.text = data.deskripsi

            recSppd.setOnClickListener {
                val intent = android.content.Intent(context, TambahSppdActivity::class.java)
                intent.putExtra("id", data.id)
                intent.putExtra("username", data.username)
                intent.putExtra("nama", data.nama)
                intent.putExtra("jabatan", data.jabatan)
                intent.putExtra("nip", data.nip)
                intent.putExtra("tanggal", data.tanggal)
                intent.putExtra("lokasi", data.lokasi)
                intent.putExtra("deskripsi", data.deskripsi)
                intent.putExtra("foto", data.foto)
                intent.putExtra("foto_lokasi", data.foto2)
                intent.putExtra("mode", "editmode")
                context.startActivity(intent)
            }

            ivMore.setOnClickListener {
                val popup = PopupMenu(context, ivMore)
                popup.inflate(R.menu.popup)
                popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                    if (menuItem.itemId == R.id.menunya_hapus) {
                        MaterialAlertDialogBuilder(context)
                            .setMessage("Yakin akan menghapus data ini?")
                            .setPositiveButton("Ya, Hapus...") { _, _ ->
                                hapusSppd(data.id, position)
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                    true
                }
                popup.show()
            }

            val imageUrl = AppConstants.BASE_URL + data.foto
            Log.d("FOTO_URL", imageUrl)

            val imageUrl2 = AppConstants.BASE_URL + data.foto2

            Glide.with(root.context)
                .load(imageUrl)
                .into(ivFoto)

            Glide.with(root.context)
                .load(imageUrl2)
                .into(ivFoto2)
        }
    }

    private fun hapusSppd(id: String, position: Int) {
        val queue = Volley.newRequestQueue(context)
        val url = AppConstants.HAPUS_SPPD

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                if (response.trim() == "success") {
                    list.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal hapus: $response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf("id" to id)
            }
        }
        queue.add(request)
    }


}

