package esos.MobiBench

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import esos.MobiBench.databinding.RowBinding

class UserAdapter(val items:ArrayList<UserData>) : RecyclerView.Adapter<UserAdapter.ViewHolder>(){

    inner class ViewHolder(val binding: RowBinding) : RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = RowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            TextViewInfo.text = "사용 시간"
            apptime.text = items[position].apptime
            apppackagename.text = items[position].apppackname
            imageView.setImageDrawable(items[position].appicon)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}