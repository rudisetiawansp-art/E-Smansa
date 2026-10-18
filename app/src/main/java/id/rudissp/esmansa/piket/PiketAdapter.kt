package id.rudissp.esmansa.piket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.rudissp.esmansa.R

class PiketAdapter(private val list: List<PiketModel>) :
    RecyclerView.Adapter<PiketAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNama: TextView = view.findViewById(R.id.txtNama)
        val txtTanggal: TextView = view.findViewById(R.id.txtTanggal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_piket, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.txtNama.text = data.nama
        holder.txtTanggal.text = data.tanggal
    }

    override fun getItemCount() = list.size
}
data class PiketModel(
    val id: String,
    val nama: String,
    val tanggal: String
)