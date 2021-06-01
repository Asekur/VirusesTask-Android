package com.example.viruses.models

class Session {
    var virus = Virus()
    var transfer = ArrayList<Virus>()
    companion object {
        val shared = Session()
    }
}