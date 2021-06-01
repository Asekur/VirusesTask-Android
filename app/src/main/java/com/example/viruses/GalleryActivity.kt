package com.example.viruses

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.example.viruses.models.Session
import com.example.viruses.models.Virus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.fragment_collection.*
import kotlinx.android.synthetic.main.virus_block.view.*
import kotlinx.android.synthetic.main.virus_gallery.view.*
import java.util.*

class GalleryActivity : LocaleAwareCompatActivity() {
    private lateinit var virusUid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        supportActionBar?.hide()
        collectionGalleryView.layoutManager = GridLayoutManager(this, 3)

        virusUid = intent.getStringExtra("uid").toString()

        imageAdd.isVisible = virusUid == Session.shared.virus.uid
        imageAdd.setOnClickListener {
            Log.d("GalleryActivity", "Selected photo...")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        fetchGallery()
    }

    var selectedPhotoGallery: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("GalleryActivity", "Photo was selected")

            selectedPhotoGallery = data.data
            val uid = Session.shared.virus.uid
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/gallery/${uid}/${filename}")
            if (selectedPhotoGallery != null) {
                ref.putFile(selectedPhotoGallery!!)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {
                            Log.d("GalleryActivity", it.toString())
                            val refDatabase = FirebaseDatabase.getInstance().getReference("/gallery/${uid}")
                            val hashMap = hashMapOf<String, Any>(
                                filename to it.toString()
                            )
                            refDatabase.updateChildren(hashMap)
                            fetchGallery()
                        }
                    }
                    .addOnFailureListener {
                    }
            }
        }
    }

    private fun fetchGallery() {
        val ref = FirebaseDatabase.getInstance().getReference("/gallery/${virusUid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    val photo = it.value.toString()
                    if (photo != "") {
                        adapter.add(PhotoItem(photo))
                    }
                }

                collectionGalleryView.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

class PhotoItem(private val url: String) : Item<GroupieViewHolder>() {
    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        //for each virus in list
        Picasso.get().load(url).into(viewHolder.itemView.photoGallery)
    }

    override fun getLayout(): Int {
        return R.layout.virus_gallery
    }
}