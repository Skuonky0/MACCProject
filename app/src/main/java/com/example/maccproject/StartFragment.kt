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
import kotlin.system.exitProcess

class StartFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(user == null){
            exitProcess(0)
        }
        val startView = inflater.inflate(R.layout.fragment_start, container, false)
        startView.keepScreenOn = true
        return startView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<LinearLayout>(R.id.menu_show)?.visibility = View.GONE
        pause = 0

        activity?.findViewById<TextView>(R.id.user_name)?.text = user?.nickname+"#"+user?.id
        activity?.findViewById<Button>(R.id.startButton)?.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.gameFragment)
        }

        activity?.findViewById<Button>(R.id.leaderb_btn)?.setOnClickListener {
            val frgmt = this

            GlobalScope.launch(Dispatchers.IO) {
                var ret: JSONArray? = null
                val c1 = async {
                    ret = Proxy.getLeaderboard()
                }
                c1.await()
                leaderboard = LeaderboardEntry(ret).array
                withContext(Dispatchers.Main){
                    NavHostFragment.findNavController(frgmt).navigate(R.id.leaderboardFragment)
                }
            }
        }
    }
}