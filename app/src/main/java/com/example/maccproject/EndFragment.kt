package com.example.maccproject

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

class EndFragment : Fragment() {
    var points : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val endView = inflater.inflate(R.layout.fragment_end, container, false)
        endView.keepScreenOn = true

        val bundle = this.arguments
        if(bundle != null){
            points = bundle.getInt("points")
        }
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            var ret: JSONObject? = null
            val c1 = async {
                ret = user?.googleId?.let { Proxy.getUserScore(it) }
            }
            c1.await()
            if(ret?.get("points") as Int <= points){
                val c2 = async {
                    ret = user?.googleId?.let { Proxy.addScore(it, points) }
                }
                c2.await()
            }
        }
        return endView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<Button>(R.id.endbtn)?.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.gameFragment)
        }
        activity?.findViewById<TextView>(R.id.points)?.text = points.toString()

        activity?.findViewById<Button>(R.id.leaderb_btn_end)?.setOnClickListener {
            val frgmt = this
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                var ret: JSONArray? = null
                val c1 = async {
                    ret = Proxy.getLeaderboard()
                }
                c1.await()
                leaderboard = LeaderboardEntry(ret)
                withContext(Dispatchers.Main){
                    NavHostFragment.findNavController(frgmt).navigate(R.id.leaderboardFragment)
                }
            }
        }
    }
}