package com.example.viruses

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.viruses.models.Constants
import com.example.viruses.models.Session
import com.example.viruses.models.Virus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.virus_block.view.*
import java.util.*

class EditActivity : LocaleAwareCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        supportActionBar?.hide()

        editingValues()

        buttonSelectPhotoEdit.setOnClickListener {
            Log.d("EditActivity", "Selected photo...")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        imageSave.setOnClickListener {
            uploadImageToFirebaseEdit()
        }
    }

    var selectedPhotoUriEdit: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("EditActivity", "Photo was selected")

            selectedPhotoUriEdit = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUriEdit)
            val circlePhotoEdit = findViewById<CircleImageView>(R.id.circlePhotoEdit)
            circlePhotoEdit.setImageBitmap(bitmap)
        }
    }

    private fun editingValues() {
        editCountry.setText(Session.shared.virus.country)
        editContinent.setText(Session.shared.virus.continent)
        editDomain.setText(Session.shared.virus.domain)
        editFullname.setText(Session.shared.virus.fullName)
        editMortality.setText(Session.shared.virus.mortality)
        editVideolink.setText(Session.shared.virus.video_link)
        editPass.setText(Session.shared.virus.password)
        editYear.setText(Session.shared.virus.year)
        Picasso.get().load(Session.shared.virus.photo_link).into(circlePhotoEdit)
    }

    private fun saveData(photoLink: String) {
        val fullName = findViewById<EditText>(R.id.editFullname).text.toString()
        val password = findViewById<EditText>(R.id.editPass).text.toString()
        if (fullName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter password and name!", Toast.LENGTH_SHORT).show()
            return
        }

        if (fullName == Session.shared.virus.fullName) {
            uploadVirusEdit(photoLink, fullName, password)
        } else {
            //проверка на существование в БД по fullName
            val checkExist = FirebaseDatabase.getInstance().getReference("Viruses")
            checkExist.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    val exists = snapshot.children.find {
                        val virus = it.getValue(Virus::class.java)
                        fullName == virus?.fullName
                    }
                    if (exists != null) {
                        Log.d("EditActivity", exists.toString())
                        Toast.makeText(this@EditActivity, "User already exists!", Toast.LENGTH_SHORT).show()
                    } else {
                        uploadVirusEdit(photoLink, fullName, password)
                    }
                }
            })
        }
    }


    private fun uploadVirusEdit(photoLink: String, fullName: String, password: String) {
        val country = when (findViewById<EditText>(R.id.editCountry).text.toString() == "") {
            true -> "Country"
            false -> findViewById<EditText>(R.id.editCountry).text.toString()
        }
        val continent = when (findViewById<EditText>(R.id.editContinent).text.toString() == "") {
            true -> "Continent"
            false -> findViewById<EditText>(R.id.editContinent).text.toString()
        }
        val year = when (findViewById<EditText>(R.id.editYear).text.toString() == "") {
            true -> "Year"
            false -> findViewById<EditText>(R.id.editYear).text.toString()
        }
        val domain = when (findViewById<EditText>(R.id.editDomain).text.toString() == "") {
            true -> "Domain"
            false -> findViewById<EditText>(R.id.editDomain).text.toString()
        }
        val mortality = when (findViewById<EditText>(R.id.editMortality).text.toString() == "") {
            true -> "Mortality"
            false -> findViewById<EditText>(R.id.editMortality).text.toString()
        }
        val videolink = when (findViewById<EditText>(R.id.editVideolink).text.toString() == "") {
            true -> "https://archive.org/download/ElephantsDream/ed_1024.mp4"
            false -> findViewById<EditText>(R.id.editVideolink).text.toString()
        }

        val hashMap = hashMapOf<String, Any>(
                "uid" to Session.shared.virus.uid,
                "fullName" to fullName,
                "country" to country,
                "continent" to continent,
                "year" to year,
                "video_link" to videolink,
                "photo_link" to photoLink,
                "mortality" to mortality,
                "domain" to domain,
                "password" to password
        )

        Session.shared.virus = Virus (Session.shared.virus.uid,
                    fullName,
                    country,
                    continent,
                    year,
                    videolink,
                    photoLink,
                    mortality,
                    domain,
                    password)

        val ref = FirebaseDatabase.getInstance().getReference("/Viruses/${Session.shared.virus.uid}")
        ref.updateChildren(hashMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data updated!", Toast.LENGTH_SHORT).show()
                }
                .addOnCanceledListener {
                    Toast.makeText(this, "Failed to update date!", Toast.LENGTH_SHORT).show()
                }
    }

    private fun uploadImageToFirebaseEdit() {
        if (selectedPhotoUriEdit == null) {
            saveData(Session.shared.virus.photo_link)
        } else {
            val filename = UUID.randomUUID().toString()

            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectedPhotoUriEdit!!).addOnSuccessListener {
                Log.d("EditActivity", "Image upload")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("EditActivity", "File location: $it")
                    saveData(it.toString())
                }
            }
        }
    }
}