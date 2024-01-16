package com.example.texteditortest.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.texteditortest.databinding.LayoutBoardListItemBinding
import com.example.texteditortest.model.BoardData

class BoardAdapter(
    private val context: Context,
    private val onItemClick : (item: BoardData) -> Unit
): ListAdapter<BoardData, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BoardData>() {
            override fun areItemsTheSame(
                oldItem: BoardData,
                newItem: BoardData
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: BoardData,
                newItem: BoardData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class BoardViewHolder(private val binding: LayoutBoardListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BoardData, onItemClick : (BoardData) -> Unit) {
            binding.item = item

            binding.boardRoot.setOnClickListener {
                onItemClick(item)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutBoardListItemBinding.inflate(inflater, parent, false)
        return BoardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as BoardViewHolder).bind(getItem(position) as BoardData, onItemClick)
    }
}