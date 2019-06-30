package com.doubleshadow.fiw

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView

class LocationAdapter(context: Context, locationList: MutableList<Location>) : BaseAdapter() {

    private val _inflater: LayoutInflater = LayoutInflater.from(context)
    private var _locationList = locationList

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val locationId: String = _locationList.get(position).locationId as String
        val locationName: String = _locationList.get(position).locationName as String
        val lat: Double = _locationList.get(position).lat as Double
        val lgn: Double = _locationList.get(position).lgn as Double

        val view: View
        val listRowHolder: ListRowHolder

        if (convertView == null) {
            view = _inflater.inflate(R.layout.location_row, parent, false)
            listRowHolder = ListRowHolder(view)
            view.tag = listRowHolder
        } else {
            view = convertView
            listRowHolder = view.tag as ListRowHolder
        }

        listRowHolder.location_name.text = locationName

        return view
    }

    override fun getItem(index: Int): Any {
        return _locationList.get(index)
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getCount(): Int {
        return _locationList.size
    }

    private class ListRowHolder(row: View?) {
        val location_name: TextView = row!!.findViewById(R.id.location) as TextView
        val remove: ImageButton = row!!.findViewById(R.id.btnRemove) as ImageButton
    }
}