package com.example.viruses

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.viruses.models.Session
import com.example.viruses.models.Virus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity
import kotlinx.android.synthetic.main.activity_filter.*

class FilterActivity : LocaleAwareCompatActivity() {
    var domainFilter = "Virus"
    var mortalityFilter = 0
    var yearFilter = 1800

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        supportActionBar?.hide()

        seekBarDomain.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("WrongConstant", "SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 1)  {
                    textViewDomain.text = getString(R.string.filterVirus)
                    domainFilter = "Virus"
                }  else {
                    textViewDomain.text = getString(R.string.filterBacteria)
                    domainFilter = "Bacteria"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        seekBarMortality.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("WrongConstant", "SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textViewMortality.text = "$progress%"
                mortalityFilter = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        seekBarYear.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("WrongConstant", "SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val resultYear = progress * 2 + 1800
                textViewYear.text = "$resultYear"
                yearFilter = resultYear
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        buttonUseFilters.setOnClickListener {
            findFilter()
        }
    }

    private fun findFilter() {
        val checkExist = FirebaseDatabase.getInstance().getReference("Viruses")
        checkExist.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                val exists = snapshot.children.filter {
                    val virus = it.getValue(Virus::class.java)
                    domainFilter == virus?.domain &&
                    yearFilter < virus.year.toInt() &&
                    mortalityFilter < virus.mortality.replace("%", "").toInt()
                }
                if (exists.isNotEmpty()) {
                    val arrayViruses = ArrayList<Virus>()
                    exists.forEach {
                        val virus = it.getValue(Virus::class.java)
                        if (virus != null) {
                            arrayViruses.add(virus)
                        }
                    }
                    Session.shared.transfer = arrayViruses
                    val intent = Intent(this@FilterActivity, MapResultActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@FilterActivity, "No such viruses", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}