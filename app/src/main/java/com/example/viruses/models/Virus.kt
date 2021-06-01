package com.example.viruses.models

import android.widget.EditText
import com.example.viruses.R

class Virus(val uid: String,
            val fullName: String,
            val country: String,
            val continent: String,
            val year: String,
            val video_link: String,
            val photo_link: String,
            val mortality: String,
            val domain: String,
            val password: String) {
    constructor() : this("", "", "", "", "", "",
            "", "", "", "")
}