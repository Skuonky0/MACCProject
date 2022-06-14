package com.example.maccproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.maccproject.LeaderboardAdapter.*
import org.json.JSONObject

class LeaderboardAdapter (private val dataset: LeaderboardEntry)
    : RecyclerView.Adapter<LeaderboardViewHolder>() {

    class LeaderboardViewHolder(private val view: View): RecyclerView.ViewHolder(view){
        fun bindItems(entry: JSONObject){
            val nickname: TextView = view.findViewById(R.id.nickname_l)
            val points: TextView = view.findViewById(R.id.points_l)
            nickname.text = entry["nickname"].toString()+"#"+entry["id"].toString()
            points.text = entry["points"].toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_model, parent, false)
        return LeaderboardViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bindItems(dataset.array[position])
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}