package ca.sfu.minerva.ui.home

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import ca.sfu.minerva.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class EventAdapter (private val context: Context, var eventList: ArrayList<Event>) : BaseAdapter(){
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
        val btnAddToCalendar = view.findViewById(R.id.btnAddToCalendar) as Button
        val tvDesc = view.findViewById(R.id.tvDesc) as TextView
        val tvDate = view.findViewById(R.id.tvDate) as TextView
        val tvLocation = view.findViewById(R.id.tvLocation) as TextView

        tvTitle.text = eventList[position].title
        tvDesc.text = eventList[position].description
        tvLocation.text = eventList[position].location

        val sdfDate = SimpleDateFormat("MMM d", Locale.getDefault())
        val sdfTime = SimpleDateFormat("hh:mm aa", Locale.getDefault())

        val startDate = sdfDate.format(eventList[position].startTime)
        val endDate = sdfDate.format(eventList[position].endTime)
        val startTime = sdfTime.format(eventList[position].startTime)
        val endTime = sdfTime.format(eventList[position].endTime)

        if(startDate.equals(endDate)){
            tvDate.text = "${startDate}, ${startTime} - ${endTime}"
        } else {
            tvDate.text = "${startDate} - ${endDate}, ${startTime} - ${endTime}"
        }

        btnAddToCalendar.setOnClickListener(){insertEventIntent(eventList[position])}


        return view
    }

    private fun insertEventIntent(event: Event){
        val startTime = event.startTime
        val endTime = event.endTime

        val intent = Intent(Intent.ACTION_INSERT)
            .putExtra(CalendarContract.Events.TITLE, event.title)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(
                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                startTime.time
            )
            .putExtra(
                CalendarContract.EXTRA_EVENT_END_TIME,
                endTime.time
            )
            .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
            .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            .putExtra(CalendarContract.Events.ALL_DAY, event.allDay)

        context.startActivity(intent)
    }

}