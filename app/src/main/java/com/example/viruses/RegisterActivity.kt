package com.example.viruses

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.viruses.models.Constants
import com.example.viruses.models.Virus
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class RegisterActivity : LocaleAwareCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        val buttonRegister = findViewById<Button>(R.id.buttonRegister)
        val btnSelectPhoto = findViewById<Button>(R.id.buttonSelectPhoto)

        btnSelectPhoto.setOnClickListener {
            Log.d("RegisterActivity", "Selected photo...")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        buttonRegister.setOnClickListener {
            uploadImageToFirebase()
        }
    }

    var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            val circlePhoto = findViewById<CircleImageView>(R.id.circlePhoto)
            circlePhoto.setImageBitmap(bitmap)

            findViewById<Button>(R.id.buttonSelectPhoto).alpha = 0f
        }
    }

    private fun createVirus(photoUrl: String) {
        val fullName = findViewById<EditText>(R.id.editNewFullname).text.toString()
        val password = findViewById<EditText>(R.id.editNewPassword).text.toString()

        if (fullName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter password and name!", Toast.LENGTH_SHORT).show()
            return
        }

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
                    Log.d("RegisterActivity", exists.toString())
                    Toast.makeText(this@RegisterActivity, "User already exists!", Toast.LENGTH_SHORT).show()
                } else {
                    uploadVirus(photoUrl, fullName, password)
                }
            }
        })
    }

    private fun uploadVirus(photoUrl: String, fullName: String, password: String) {
        val country = when (findViewById<EditText>(R.id.editNewCountry).text.toString() == "") {
            true -> "Country"
            false -> findViewById<EditText>(R.id.editNewCountry).text.toString()
        }
        val continent = when (findViewById<EditText>(R.id.editNewContinent).text.toString() == "") {
            true -> "Continent"
            false -> findViewById<EditText>(R.id.editNewContinent).text.toString()
        }
        val year = when (findViewById<EditText>(R.id.editNewYear).text.toString() == "") {
            true -> "Year"
            false -> findViewById<EditText>(R.id.editNewYear).text.toString()
        }
        val domain = when (findViewById<EditText>(R.id.editNewDomain).text.toString() == "") {
            true -> "Domain"
            false -> findViewById<EditText>(R.id.editNewDomain).text.toString()
        }
        val mortality = when (findViewById<EditText>(R.id.editNewMortality).text.toString() == "") {
            true -> "Mortality"
            false -> findViewById<EditText>(R.id.editNewMortality).text.toString()
        }
        val uuid = UUID.randomUUID().toString()

        val virus = Virus(uuid,
                fullName,
                country,
                continent,
                year,
                "https://archive.org/download/ElephantsDream/ed_1024.mp4",
                photoUrl,
                mortality,
                domain,
                password)

        val ref = FirebaseDatabase.getInstance().getReference("Viruses").child(uuid)
                .setValue(virus)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Success")
                    Toast.makeText(this@RegisterActivity, "Created!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, TabActivity::class.java)
                    startActivity(intent)
                }
    }

    private fun uploadImageToFirebase() {
        if (selectedPhotoUri == null) {
            createVirus(Constants.defaultImage)
        } else {
            val filename = UUID.randomUUID().toString()

            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
                Log.d("RegisterActivity", "Image upload")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File location: $it")
                    createVirus(it.toString())
                }
            }
        }
    }
}