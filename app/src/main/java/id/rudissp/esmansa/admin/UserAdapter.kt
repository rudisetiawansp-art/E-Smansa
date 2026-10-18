package id.rudissp.esmansa.admin

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.rudissp.esmansa.R

class UserAdapter(
    private val context: Context,
    private val users: List<UserModel>,
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtInfo: TextView = itemView.findViewById(R.id.txtInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.txtInfo.text = user.nama

        holder.itemView.setOnClickListener {
            val intent = Intent(context, EditUserActivity::class.java).apply {
                putExtra("username", user.username)
                putExtra("mode", "edit") // atau "tambah"
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = users.size
}

data class UserModel(
    var nama: String = "",
    var username: String = ""
)