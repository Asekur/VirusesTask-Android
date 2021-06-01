package com.example.viruses

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.viruses.models.Session
import com.example.viruses.models.Virus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : LocaleAwareCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        supportActionBar?.hide()

        val uid = intent.getStringExtra("uid")
        if (uid != null) {
            setVirus(uid)
        }

        buttonGalleryDetail.setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }
    }

    private fun setVirus(uid: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/Viruses/${uid}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                val virus = snapshot.getValue(Virus::class.java)
                if (virus == null) {
                    Log.d("DetailActivity", "Something do wrong...")
                    return
                }

                textDetailFullname.text = virus.fullName + ": " + virus.domain
                textDetailLocation.text = virus.continent + ", " + virus.country
                textDetailMortality.text = virus.mortality
                textDetailYear.text = virus.year
                Picasso.get().load(virus.photo_link).into(circlePhotoDetail)

                videoView.setVideoPath(virus.video_link)
                videoView.start()
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }
}