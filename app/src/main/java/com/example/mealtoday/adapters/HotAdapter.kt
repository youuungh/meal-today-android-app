package com.example.mealtoday.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mealtoday.databinding.ItemHotBinding
import com.example.mealtoday.data.Hot
import com.example.mealtoday.ui.fragments.HomeFragmentDirections

class HotAdapter: RecyclerView.Adapter<HotAdapter.HotViewHolder>() {

    lateinit var onHotItemClick: ((Hot) -> Unit)

    private val diffUtil = object : DiffUtil.ItemCallback<Hot>() {
        override fun areItemsTheSame(oldItem: Hot, newItem: Hot): Boolean {
            return oldItem.idMeal == newItem.idMeal
        }

        override fun areContentsTheSame(oldItem: Hot, newItem: Hot): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    inner class HotViewHolder(val binding: ItemHotBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotViewHolder {
        return HotViewHolder(ItemHotBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: HotViewHolder, position: Int) {
        val data = differ.currentList[position]
        Glide.with(holder.itemView)
            .load(data.strMealThumb)
            .override(300, 300)
            .into(holder.binding.hotImage)

        holder.apply {
            itemView.transitionName = "trans_${data.idMeal}"
            itemView.setOnClickListener {
                //onHotItemClick.invoke(data)
                val extras = FragmentNavigatorExtras(holder.binding.cvHotImage to "trans_${data.idMeal}")
                Navigation.findNavController(it).navigate(
                    HomeFragmentDirections.actionHomeFragmentToMealFragment(
                        data.idMeal, data.strMealThumb, data.strMeal), extras
                )
            }
        }
    }
}