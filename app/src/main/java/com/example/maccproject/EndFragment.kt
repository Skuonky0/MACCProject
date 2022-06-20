package com.example.maccproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.system.exitProcess

class EndFragment : Fragment() {
    var points : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(user == null){
            exitProcess(0)
        }

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
        this.activity?.findViewById<LinearLayout>(R.id.menu_show)?.visibility = View.GONE
        pause = 0

        activity?.findViewById<TextView>(R.id.user_name)?.text = user?.nickname+"#"+user?.id
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
                leaderboard = LeaderboardEntry(ret).array
                withContext(Dispatchers.Main){
                    NavHostFragment.findNavController(frgmt).navigate(R.id.action_endFragment_to_leaderboardFragment)
                }
            }
        }
    }
}