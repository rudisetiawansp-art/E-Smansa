package id.rudissp.esmansa.link

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import id.rudissp.esmansa.R


class LinkAdapter(private var items: ArrayList<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
    }

    // Fungsi update data
    fun updateData(newItems: ArrayList<Any>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // Menentukan tipe tampilan berdasarkan isi datanya
    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) {
            VIEW_TYPE_HEADER // Jika datanya berupa Teks String, jadikan Header
        } else {
            VIEW_TYPE_ITEM   // Jika datanya berupa LinkModel, jadikan Kartu Link
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            // Gunakan layout header
            val view = inflater.inflate(R.layout.item_header_kategori, parent, false)
            HeaderViewHolder(view)
        } else {
            // Gunakan layout item link (Pastikan Anda sudah punya file item_link.xml)
            val view = inflater.inflate(R.layout.item_link, parent, false)
            LinkViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            val judulHeader = items[position] as String
            holder.tvHeaderJudul.text = judulHeader
        } else if (holder is LinkViewHolder) {
            // Set data link
            val data = items[position] as LinkModel

            holder.tvJudul.text = data.judul
            holder.tvUrl.text = data.link

            if (data.kategori.equals("Sosial Media", ignoreCase = true)) {
                when (data.platform.lowercase()) {
                    "instagram" -> holder.ivIcon.setImageResource(R.drawable.instagram)
                    "youtube" -> holder.ivIcon.setImageResource(R.drawable.yt)
                    "facebook" -> holder.ivIcon.setImageResource(R.drawable.fb)
                    "tiktok" -> holder.ivIcon.setImageResource(R.drawable.tiktok)

                    // Tambahkan ikon lain sesuai kebutuhan
                    else -> holder.ivIcon.setImageResource(R.drawable.ic_webb)
                }
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_webb)
            }

            // Aksi klik membuka link di browser
            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                var urlTujuan = data.link.trim()

                if (!urlTujuan.startsWith("http://") && !urlTujuan.startsWith("https://")) {
                    urlTujuan = "http://$urlTujuan"
                }

                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(urlTujuan)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Gagal membuka link. Cek format URL.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // --- VIEW HOLDERS ---
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHeaderJudul: TextView = itemView.findViewById(R.id.tv_header_judul)
    }

    inner class LinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Sesuaikan ID ini dengan yang ada di item_link.xml Anda
        val tvJudul: TextView = itemView.findViewById(R.id.tv_judul_link)
        val tvUrl: TextView = itemView.findViewById(R.id.tv_url_link)
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon_link)
    }
}

data class LinkModel(
    val id: String,
    val judul: String,
    val link: String,
    val platform: String,
    val kategori: String
)