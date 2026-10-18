package id.rudissp.esmansa.jurtu

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request.Method
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ItemJurtuBinding

data class JurtuModel(
    val id: String,
    val nama: String,
    val tanggal: String,
    val jam1: String,
    val jam2: String,
    val rincian: String,
    val foto: String
)

class JurtuAdapter(
    private val context: Context, private val list: MutableList<JurtuModel>
) : RecyclerView.Adapter<JurtuAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemJurtuBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJurtuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        with(holder.binding) {
            jurtuItemNama.text = data.nama
            jurtuItemTanggal.text = data.tanggal + " (" + data.jam1 + " - " + data.jam2 + ")"
            jurtuItemKegiatan.text = data.rincian

            recJurtu.setOnClickListener {
                val intent = android.content.Intent(context, TambahJurtu::class.java)
                intent.putExtra("id", data.id)
                intent.putExtra("nama", data.nama)
                intent.putExtra("tanggal", data.tanggal)
                intent.putExtra("jam1", data.jam1)
                intent.putExtra("jam2", data.jam2)
                intent.putExtra("rincian", data.rincian)
                intent.putExtra("foto", data.foto)
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
                                hapusJurtu(data.id, position)
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                    true
                }
                popup.show()
            }

            val imageUrl = AppConstants.BASE_URL + data.foto
            Glide.with(root.context)
                .load(imageUrl)
                .into(ivFoto)
        }
    }

    private fun hapusJurtu(id: String, position: Int) {
        val queue = Volley.newRequestQueue(context)
        val url = AppConstants.HAPUS_JURTU

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

