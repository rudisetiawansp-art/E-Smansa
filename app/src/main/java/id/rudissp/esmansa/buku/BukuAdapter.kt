package id.rudissp.esmansa.buku

import android.R.attr.data
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
import id.rudissp.esmansa.databinding.ItemBukuBinding
import org.json.JSONException
import org.json.JSONObject

class BukuAdapter(
    private val context: Context,
    private val listBuku: ArrayList<BukuModel>
) :
    RecyclerView.Adapter<BukuAdapter.BukuViewHolder>() {

    private val baseUrlFoto = "https://sman1sukorejo.sch.id/elgrassio/uploads/"

    inner class BukuViewHolder(val binding: ItemBukuBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BukuViewHolder {
        val binding = ItemBukuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BukuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BukuViewHolder, position: Int) {
        val buku = listBuku[position]

        with(holder.binding) {
            tvJudulBuku.text = buku.judul
            tvNobuku.text = "No: ${buku.nobuku}"
            tvPenulis.text = buku.penulis
            tvStok.text = "Jumlah: ${buku.jumlah}"

            val urlLengkap = baseUrlFoto + buku.fotoDepan
            Glide.with(holder.itemView.context)
                .load(urlLengkap)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(ivCoverBuku)

            recbuku.setOnClickListener {
                val intent = android.content.Intent(context, TambahBuku::class.java)
                intent.putExtra("nobuku", buku.nobuku)
                intent.putExtra("judul", buku.judul)
                intent.putExtra("penulis", buku.penulis)
                intent.putExtra("jumlah", buku.jumlah)
                intent.putExtra("foto_depan", buku.fotoDepan)
                intent.putExtra("foto_samping", buku.fotoSamping)
                intent.putExtra("tanggalbeli", buku.tanggalbeli)
                intent.putExtra("penerbit", buku.penerbit)
                intent.putExtra("harga", buku.harga)
                intent.putExtra("sumberdana", buku.sumberdana)
                intent.putExtra("jenis", buku.jenis)
                intent.putExtra("mode", "editmode")
                context.startActivity(intent)
            }

            itemBukuMore.setOnClickListener {
                val popup = PopupMenu(context, itemBukuMore)
                popup.inflate(R.menu.popup)
                popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                    if (menuItem.itemId == R.id.menunya_hapus) {
                        MaterialAlertDialogBuilder(context)
                            .setMessage("Yakin akan menghapus buku ini?")
                            .setPositiveButton("Ya, Hapus...") { _, _ ->
                                hapusbuku(buku.nobuku, position)
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                    true
                }
                popup.show()
            }

        }
    }

    override fun getItemCount(): Int {
        return listBuku.size
    }

    fun updateData(newList: ArrayList<BukuModel>) {
        listBuku.clear()
        listBuku.addAll(newList)
        notifyDataSetChanged()
    }

    private fun hapusbuku(nobuku: String, position: Int) {
        val queue = Volley.newRequestQueue(context)
        val url = AppConstants.HAPUS_BUKU

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    // Parsing response string menjadi JSONObject
                    val jsonObject = JSONObject(response)
                    val status = jsonObject.getString("status")

                    if (status == "success") {
                        listBuku.removeAt(position)
                        notifyItemRemoved(position)

                        val message = jsonObject.optString("message", "Data berhasil dihapus")
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    } else {
                        val message = jsonObject.optString("message", "Gagal menghapus data")
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Gagal memproses respons server: $response", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(context, "Error koneksi: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf("nobuku" to nobuku)
            }
        }
        queue.add(request)
    }
}

data class BukuModel(
    val nobuku: String,
    val tanggalbeli: String,
    val penerbit: String,
    val harga: String,
    val sumberdana: String,
    val jenis: String,
    val judul: String,
    val penulis: String,
    val jumlah: String,
    val fotoDepan: String,
    val fotoSamping: String

)