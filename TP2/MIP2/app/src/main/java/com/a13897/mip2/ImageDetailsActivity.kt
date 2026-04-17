package com.a13897.mip2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.a13897.mip2.databinding.ActivityImageDetailsBinding
import com.bumptech.glide.Glide

class ImageDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("EXTRA_URL") ?: ""
        val author = intent.getStringExtra("EXTRA_AUTHOR") ?: "Unknown Author"
        val id = intent.getStringExtra("EXTRA_ID") ?: "N/A"
        val width = intent.getIntExtra("EXTRA_WIDTH", 0)
        val height = intent.getIntExtra("EXTRA_HEIGHT", 0)

        // Bind data to UI
        binding.detailAuthorTextView.text = author
        binding.detailIdTextView.text = "Image ID: $id"
        binding.detailDimensTextView.text = "Resolution: $width x $height"

        Glide.with(this)
            .load(url)
            .placeholder(android.R.color.darker_gray)
            .into(binding.detailImageView)
    }
}
