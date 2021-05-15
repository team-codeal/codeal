package com.example.utils.recyclers.teams

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.BoardActivity
import com.example.prototypefirebase.R

class TeamViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    private var teamID: String? = null

    fun bindView(name: String, des: String, teamID: String) {
        teamNameHolder.text = name
        teamDescriptionHolder.text = des
        this.teamID = teamID
    }

    fun setGestureListener(context: Context, onTeamClickListenerCallback: (Int) -> Unit) {
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?) = true
            override fun onScroll(
                e: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                val dx = e2!!.x - e!!.x
                Log.d("fuckShit", dx.toString())
                if (dx > 100) {
                    if (teamID == null) {
                        return true
                    }
                    val intent = Intent(context, BoardActivity::class.java)
                    intent.putExtra("TeamID", teamID)
                    startActivity(context, intent, null)
                }
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                onTeamClickListenerCallback(bindingAdapterPosition)
                return true
            }
        })
        itemView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    fun unsetGestureListener() {
        itemView.setOnTouchListener(null)
    }

    private val teamNameHolder: TextView = itemView.findViewById(R.id.team_name)
    private val teamDescriptionHolder: TextView = itemView.findViewById(R.id.team_short_desc)

}