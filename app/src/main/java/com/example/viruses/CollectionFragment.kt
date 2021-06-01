package com.example.viruses

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viruses.models.Session
import com.example.viruses.models.Virus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_collection.*
import kotlinx.android.synthetic.main.virus_block.view.*

class CollectionFragment : Fragment() {

    var viruses = ArrayList<Virus>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collection, container, false)
    }

    override fun onResume() {
        super.onResume()
        fetchViruses()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectionVirusesView.layoutManager = GridLayoutManager(context, 3)

        imageEdit.setOnClickListener {
            val intent = Intent(context, EditActivity::class.java)
            startActivity(intent)
        }

        imageFilter.setOnClickListener {
            val intent = Intent(context, FilterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchViruses() {
        val ref = FirebaseDatabase.getInstance().getReference("Viruses")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    Log.d("CollectionFragment", it.toString())
                    val virus = it.getValue(Virus::class.java)
                    if (virus != null) {
                        adapter.add(VirusItem(virus))
                        viruses.add(virus)
                    }
                }

                adapter.setOnItemClickListener { item, view ->
                    val intent = Intent(view.context, DetailActivity::class.java)
                    intent.putExtra("uid", viruses[adapter.getAdapterPosition(item)].uid)
                    startActivity(intent)
                }

                collectionVirusesView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

class VirusItem(val virus: Virus) : Item<GroupieViewHolder>() {
    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        //for each virus in list
        viewHolder.itemView.nameCollectionVirus.text = virus.fullName
        viewHolder.itemView.domainYearText.text = virus.domain + ", " + virus.year
        Picasso.get().load(virus.photo_link).into(viewHolder.itemView.photoProfile)
    }

    override fun getLayout(): Int {
        return R.layout.virus_block
    }
}