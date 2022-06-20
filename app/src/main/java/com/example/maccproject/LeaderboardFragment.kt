package com.example.maccproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.system.exitProcess

class LeaderboardFragment : Fragment() {

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
        val binding = inflater.inflate(R.layout.fragment_leaderboard, container, false)
        val recycler = binding.findViewById<RecyclerView>(R.id.scroll_leader)

        recycler.adapter = leaderboard?.let { LeaderboardAdapter(it) }
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.setHasFixedSize(true)
        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.activity?.findViewById<LinearLayout>(R.id.menu_show)?.visibility = View.GONE
        pause = 0

        activity?.findViewById<ProgressBar>(R.id.scoreProg)?.visibility = View.GONE
        activity?.findViewById<TextView>(R.id.user_name)?.text = user?.nickname+"#"+user?.id
        activity?.findViewById<Button>(R.id.backStart)?.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.startFragment)
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.findViewById<ImageView>(R.id.menu)?.visibility = View.VISIBLE
    }
}