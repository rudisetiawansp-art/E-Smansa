package id.rudissp.esmansa.metu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import java.io.Serializable

class MetuAdapter(
    private val context: Context,
    private val data: MutableList<Metu>,
    private val onEdit: (Metu) -> Unit,
    private val onDelete: (Metu) -> Unit
) : RecyclerView.Adapter<MetuAdapter.MetuViewHolder>() {

    lateinit var prefs : SharedPreferences

    inner class MetuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foto: ImageView = itemView.findViewById(R.id.recycler_fotonya)
        val nama: TextView = itemView.findViewById(R.id.recycler_nama)
        val kelas: TextView = itemView.findViewById(R.id.recyler_kelas)
        val jamke: TextView = itemView.findViewById(R.id.recyler_jamke)
        val keperluan: TextView = itemView.findViewById(R.id.recyler_keperluan)
        val timestamp: TextView = itemView.findViewById(R.id.recycler_timestamp)
        val dibuatOleh: TextView = itemView.findViewById(R.id.recycler_dibuatoleh)
        val more: ImageView = itemView.findViewById(R.id.recycler_more)
        val card: RelativeLayout = itemView.findViewById(R.id.cardnya)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetuViewHolder {
        prefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val view = LayoutInflater.from(context).inflate(R.layout.item_metu, parent, false)
        return MetuViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MetuViewHolder, position: Int) {
        val item = data[position]

        holder.nama.text = item.nama
        holder.kelas.text = item.kelas
        holder.jamke.text = "Jam ke: ${item.jam1} s.d. ${item.jam2}"
        holder.keperluan.text = item.alasan
        holder.timestamp.text = item.tanggal
        holder.dibuatOleh.text = "Dibuat oleh: ${item.guru}"

        Glide.with(context)
            .load(AppConstants.BASE_URL + "fotometu/" + item.foto)
            .placeholder(R.drawable.logo)
            .into(holder.foto)

        holder.more.setOnClickListener {
            showPopupMenu(it, item)
        }

        holder.foto.setOnClickListener {
            val editor = prefs.edit()
            editor.putString("fullnya_gambar", AppConstants.BASE_URL + "fotometu/" + item.foto)
            editor.apply()

            //val intent = Intent(context, Full::class.java)
            //context.startActivity(intent)
        }


        holder.card.setOnClickListener {
            onEdit(item)
        }
    }

    private fun showPopupMenu(view: View, item: Metu) {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.popup) // menu ini harus kamu buat di res/menu/menu_edit_hapus.xml
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menunya_hapus -> {
                    onDelete(item)
                    true
                }


                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int = data.size
}

data class Metu(
    val id: String,
    val nama: String,
    val kelas: String,
    val jam1: String,
    val jam2: String,
    val guru: String,
    val alasan: String,
    val tanggal: String,
    val foto: String
) : Serializable
