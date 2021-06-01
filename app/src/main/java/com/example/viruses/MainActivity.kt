package com.example.viruses

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.viruses.models.Session
import com.example.viruses.models.Virus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity

class MainActivity : LocaleAwareCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val btn = findViewById<Button>(R.id.buttonAuthorize)

        btn.setOnClickListener {
            val username = findViewById<EditText>(R.id.editPersonName).text.toString()
            val password = findViewById<EditText>(R.id.editPassword).text.toString()

            val checkExist = FirebaseDatabase.getInstance().getReference("Viruses")
            checkExist.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    val exists = snapshot.children.find {
                        val virus = it.getValue(Virus::class.java)
                        username == virus?.fullName && password == virus?.password
                    }
                    if (exists != null) {
                        Log.d("MainActivity", exists.toString())
                        val obj = exists.getValue(Virus::class.java) ?: return
                        Session.shared.virus = obj
                        val intent = Intent(this@MainActivity, TabActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, "User doesn't exist!", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        val text = findViewById<TextView>(R.id.newAccountTextView)
        text.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}