package com.notes.notesproxmlviews

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {
    private var addNoteBtn: FloatingActionButton? = null
    private var recyclerView: RecyclerView? = null
    private var menuBtn: ImageButton? = null
    private lateinit var noteAdapter: NoteAdapter
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addNoteBtn = findViewById(R.id.add_note_btn)
        recyclerView = findViewById(R.id.recyler_view)
        menuBtn = findViewById(R.id.menu_btn)

        setupRecyclerView()

        addNoteBtn!!.setOnClickListener {
            startActivity(Intent(this@MainActivity, NoteDetailsActivity::class.java))
        }
        menuBtn!!.setOnClickListener { showMenu() }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(this)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = noteAdapter
    }

    override fun onStart() {
        super.onStart()
        setupFirestoreListener()
    }

    override fun onStop() {
        super.onStop()
        listenerRegistration?.remove()
    }

    private fun setupFirestoreListener() {
        // Query to get notes sorted by timestamp descending
        val query = Utility.getCollectionReferenceForNotes()
            .orderBy("timestamp", Query.Direction.DESCENDING)

        listenerRegistration = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Utility.showToast(this, "Error loading notes: ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val notes = mutableListOf<Note>()
                val docIds = mutableListOf<String>()
                for (document in snapshot.documents) {
                    val note = document.toObject(Note::class.java)
                    if (note != null) {
                        notes.add(note)
                        docIds.add(document.id)
                    }
                }
                noteAdapter.updateData(notes, docIds)
            }
        }
    }

    private fun showMenu() {
        val popupMenu = PopupMenu(this@MainActivity, menuBtn)
        popupMenu.menu.add("Logout")
        popupMenu.setOnMenuItemClickListener { item ->
            if (item.title == "Logout") {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                true
            } else {
                false
            }
        }
        popupMenu.show()
    }
}