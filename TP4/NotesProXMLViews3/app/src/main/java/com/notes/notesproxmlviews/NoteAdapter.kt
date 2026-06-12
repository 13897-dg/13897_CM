package com.notes.notesproxmlviews

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private val context: Context,
    private var noteList: List<Note> = emptyList(),
    private var docIds: List<String> = emptyList()
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    fun updateData(newNoteList: List<Note>, newDocIds: List<String>) {
        this.noteList = newNoteList
        this.docIds = newDocIds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        val docId = docIds[position]

        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content
        holder.timestampTextView.text = if (note.timestamp != null) {
            Utility.timestampToString(note.timestamp)
        } else {
            ""
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, NoteDetailsActivity::class.java).apply {
                putExtra("title", note.title)
                putExtra("content", note.content)
                putExtra("docId", docId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.note_title_text_view)
        val contentTextView: TextView = itemView.findViewById(R.id.note_content_text_view)
        val timestampTextView: TextView = itemView.findViewById(R.id.note_timestamp_text_view)
    }
}
