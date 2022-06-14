package com.example.maccproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.NavHostFragment
import com.example.maccproject.databinding.FragmentNicknameBinding
import kotlinx.coroutines.*
import org.json.JSONObject


class NicknameFragment : Fragment() {

    private var _binding: FragmentNicknameBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNicknameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.activity?.findViewById<Button>(R.id.button2)?.setOnClickListener {
            val nicknm = binding.nickname.text.toString()
            val scope = CoroutineScope(Dispatchers.IO)
            val frgmt = this
            scope.launch {
                var ret: JSONObject? = null
                val c1 = async {
                    ret = user?.googleId?.let { Proxy.addNickname(it, nicknm) }
                }
                c1.await()
                if(ret != null){
                    user = User(ret!!["id"]as Int, ret!!["name"] as String, ret!!["email"] as String, ret!!["googleId"] as String, ret!!["nickname"] as String)
                    withContext(Dispatchers.Main) {
                        NavHostFragment.findNavController(frgmt).navigate(R.id.gameFragment)
                    }
                }
            }
        }
    }
}