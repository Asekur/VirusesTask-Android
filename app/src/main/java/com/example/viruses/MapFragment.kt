package com.example.viruses

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.viruses.models.Session
import com.example.viruses.models.Virus
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MapFragment : Fragment() {

    var virusesMap = ArrayList<Virus>()
    var places = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchViruses()
    }

    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(p0: Marker?): View? {
                return null
            }

            override fun getInfoContents(marker: Marker?): View {
                val context: FragmentActivity? = activity

                val info = LinearLayout(context)
                info.orientation = LinearLayout.VERTICAL

                val title = TextView(context)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker?.title

                val snippet = TextView(context)
                snippet.setTextColor(Color.GRAY)
                snippet.text = marker?.snippet

                info.addView(title)
                info.addView(snippet)

                return info
            }
        })

        places.forEach { place ->
            val virusesWithTheSamePoints =
                virusesMap.filter { it.country == place }
            var title = ""
            virusesWithTheSamePoints.forEach {
                title += it.fullName + "\n"
            }

            try {
                val geocoder = Geocoder(context)
                val addresses = geocoder.getFromLocationName(place, 1)
                if (addresses.size > 0) {
                    val coordinate = LatLng(addresses[0].latitude, addresses[0].longitude)
                    val marker = googleMap.addMarker(
                        MarkerOptions().position(coordinate)
                                .title(place)
                                .snippet(title)
                                .icon(context?.let { bitmapFromVector(it, R.drawable.ic_coronavirus) })
                    )
                    marker.tag = virusesWithTheSamePoints
                    googleMap.setOnInfoWindowClickListener {
                        Session.shared.transfer = it.tag as ArrayList<Virus>
                        if (Session.shared.transfer.size == 1) {
                            val virus = Session.shared.transfer[0]
                            val intent = Intent(context, DetailActivity::class.java)
                            intent.putExtra("uid", virus.uid)
                            startActivity(intent)
                        } else {
                            val intent = Intent(context, MapResultActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            } catch (err: Exception) {
                Log.d("MapFragment", err.toString())
            }
        }
    }

    private fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable: Drawable? = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight())
        val bitmap: Bitmap? = vectorDrawable?.getIntrinsicWidth()?.let { Bitmap.createBitmap(it, vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888) }
        val canvas = bitmap?.let { Canvas(it) }
        if (canvas != null) vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun fetchViruses() {
        val ref = FirebaseDatabase.getInstance().getReference("Viruses")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    Log.d("MapFragment", it.toString())
                    val virus = it.getValue(Virus::class.java)
                    if (virus != null) {
                        virusesMap.add(virus)
                        if (!places.contains(virus.country)) {
                            places.add(virus.country)
                        }
                    }
                }
                val mapFragment =
                    childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                mapFragment?.getMapAsync(callback)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}