package com.doubleshadow.fiw

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_save.*

class SaveActivity : AppCompatActivity() {

    lateinit var mData: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save)

        mData = FirebaseDatabase.getInstance().reference

        save_location_btn.setOnClickListener {
            addLocation()
        }
    }

    fun addLocation() {
        val location = Location.create()

        // Set values to location
        location.locationName = location_name_txt.text.toString()
        location.lat = location_lat_txt.text.toString().toDouble()
        location.lgn = location_lng_txt.text.toString().toDouble()

        // Get object id for new location
        val newLocation = mData.child(Statics.FIREBASE_TASK).push()
        location.locationId = newLocation.key

        // Set values to the task in the firebase
        newLocation.setValue(location)

        // Reset edittext views
        location_name_txt.setText("")
        location_lat_txt.setText("")
        location_lng_txt.setText("")

        Toast.makeText(this, "Location added successfully" + location.locationId, Toast.LENGTH_SHORT).show()
    }

}
