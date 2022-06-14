package com.example.maccproject

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.json.JSONArray

class StartFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val startView = inflater.inflate(R.layout.fragment_start, container, false)
        startView.keepScreenOn = true
        return startView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<Button>(R.id.startButton)?.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.gameFragment)
        }

        activity?.findViewById<Button>(R.id.leaderb_btn)?.setOnClickListener {
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