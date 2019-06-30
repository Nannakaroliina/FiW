package com.doubleshadow.fiw

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_save.*
import kotlinx.android.synthetic.main.content_save.*

class SaveActivity : AppCompatActivity() {

    lateinit var mData: DatabaseReference
    lateinit var mAdapter: LocationAdapter
    var _locationList: MutableList<Location>? = null

    var _locationListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            loadLocationList(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("SaveActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_save)

        mData = FirebaseDatabase.getInstance().reference

        save_location_btn.setOnClickListener {
            addLocation()
        }

        fab.setOnClickListener {
            showFooter()
        }

        mAdapter = LocationAdapter(this, _locationList!!)
        locations_list!!.setAdapter(mAdapter)
        _locationList = mutableListOf()
        mData.orderByKey().addValueEventListener(_locationListener)
    }

    fun showFooter(){
        footer.visibility = View.VISIBLE
        fab.visibility = View.GONE
    }

    fun addLocation() {
        val location = Location.create()

        // Set values to location
        location.locationName = new_location_name.text.toString()
        val latStr = location_lat_txt.text.toString()
        location.lat = latStr.toDouble()
        val lngStr = location_lng_txt.text.toString()
        location.lgn = lngStr.toDouble()

        // Get object id for new location
        val newLocation = mData.child(Statics.FIREBASE_TASK).push()
        location.locationId = newLocation.key

        // Set values to the task in the firebase
        newLocation.setValue(location)

        // Reset edittext views
        new_location_name.setText("")
        location_lat_txt.setText("")
        location_lng_txt.setText("")

        Toast.makeText(this, "Location added successfully" + location.locationId, Toast.LENGTH_SHORT).show()
    }

    private fun loadLocationList(dataSnapshot: DataSnapshot) {
        Log.d("SaveActivity", "loadLocationList")

        val locations = dataSnapshot.children.iterator()

        // Checks if there is data on database
        if(locations.hasNext()) {

            _locationList!!.clear()

            val listIndex = locations.next()
            val itemsIterator = listIndex.children.iterator()

            // Checks if there is locations or not
            while (itemsIterator.hasNext()) {

                // Get location
                val currentItem = itemsIterator.next()
                val location = Location.create()

                val map = currentItem.getValue() as HashMap<String, Any>

                location.locationId = currentItem.key
                location.locationName = map.get("locationName") as String?
                _locationList!!.add(location)
            }
        }

        mAdapter.notifyDataSetChanged()
    }

}
