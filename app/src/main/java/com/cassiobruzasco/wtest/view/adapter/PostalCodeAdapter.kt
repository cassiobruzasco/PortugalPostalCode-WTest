package com.cassiobruzasco.wtest.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cassiobruzasco.wtest.R
import com.cassiobruzasco.wtest.data.model.PostalCodeModel
import com.cassiobruzasco.wtest.databinding.PostalItemLayoutBinding

class PostalCodeAdapter(private val action: (PostalCodeModel) -> Unit) :
    RecyclerView.Adapter<PostalCodeAdapter.PostalCodeViewHolder>() {

    var list: MutableList<PostalCodeModel> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PostalCodeViewHolder(
        PostalItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: PostalCodeViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    inner class PostalCodeViewHolder(private val binding: PostalItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PostalCodeModel) {
            binding.apply {
                val address = "${data.arteryType} ${data.arteryName}"
                postalCodeLabel.text = data.postalCodeComplete
                streetLabel.text =
                    if (!address.isBlank()) address else binding.root.context.getString(R.string.no_address)
                localizationLabel.text = data.localeName
                designationLabel.text = data.designationPostal

                root.setOnClickListener { action(data) }
            }
        }
    }
}