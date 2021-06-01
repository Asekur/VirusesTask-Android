package com.example.viruses

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.viruses.models.Session
import com.example.viruses.models.Virus
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity
import kotlinx.android.synthetic.main.activity_map_result.*
import kotlinx.android.synthetic.main.fragment_collection.*
import kotlinx.android.synthetic.main.virus_block.view.*

class MapResultActivity : LocaleAwareCompatActivity() {
    var virusesMap = ArrayList<Virus>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_result)
        supportActionBar?.hide()
        resultMapCollection.layoutManager = GridLayoutManager(this, 3)
        fetchVirusesMap()
    }

    private fun fetchVirusesMap() {
        val adapterMap = GroupAdapter<GroupieViewHolder>()
        Session.shared.transfer.forEach {
            Log.d("MapResultActivity", it.toString())
            adapterMap.add(VirusItemMap(it))
            virusesMap.add(it)
        }

        adapterMap.setOnItemClickListener { item, view ->
            val intent = Intent(view.context, DetailActivity::class.java)
            intent.putExtra("uid", virusesMap[adapterMap.getAdapterPosition(item)].uid)
            startActivity(intent)
        }

        resultMapCollection.adapter = adapterMap
    }
}

class VirusItemMap(val virus: Virus) : Item<GroupieViewHolder>() {
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