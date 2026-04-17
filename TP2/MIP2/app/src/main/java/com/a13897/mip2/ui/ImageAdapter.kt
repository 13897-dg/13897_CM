package com.a13897.mip2.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a13897.mip2.databinding.ItemImageBinding
import com.a13897.mip2.model.ImageItem
import com.bumptech.glide.Glide

class ImageAdapter(private var images: List<ImageItem> = emptyList()) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    fun submitList(newImages: List<ImageItem>) {
        images = newImages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = images[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = images.size

    class ImageViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ImageItem) {
            binding.authorTextView.text = item.author
            Glide.with(binding.root.context)
                .load(item.download_url)
                .placeholder(android.R.color.darker_gray)
                .into(binding.imageView)
        }
    }
}
