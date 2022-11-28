package ca.sfu.minerva.ui.home

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ca.sfu.minerva.R


class EventAdapter (private val context: Context, private var eventList: List<Event>) : BaseAdapter(){
    override fun getCount(): Int {
        return eventList.size
    }

    override fun getItem(position: Int): Any {
        return eventList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.event_adapter,null)
        val tvTitle = view.findViewById(R.id.tvTitle) as TextView
//        val tvDesc = view.findViewById(R.id.tvDesc) as TextView
//        val tvDate = view.findViewById(R.id.tvDate) as TextView

        tvTitle.text = eventList[position].title
//        tvDesc.text = eventList[position].description
//        tvDate.text = "${eventList[position].startTime} - ${eventList[position].endTime}"

        return view
    }

}