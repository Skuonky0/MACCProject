package com.example.maccproject

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import org.json.JSONObject


var initialTilt = 0f
var user: User? = null
var leaderboard = mutableListOf<JSONObject>()
var pause = 0
var ingame = 0
var mGoogleSignInClient : GoogleSignInClient? = null
var sound = 1

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        val menu_show = findViewById<LinearLayout>(R.id.menu_show)
        findViewById<ImageView>(R.id.menu).setOnClickListener {
            if(ingame == 1){
                findViewById<TextView>(R.id.calibrate).visibility = View.VISIBLE
                findViewById<Button>(R.id.cal_btn).visibility = View.VISIBLE
            }
            else{
                findViewById<TextView>(R.id.calibrate).visibility = View.GONE
                findViewById<Button>(R.id.cal_btn).visibility = View.GONE
            }
            if(menu_show.visibility == View.VISIBLE){
                menu_show.visibility = View.GONE
                pause = 0
            }
            else{
                menu_show.visibility = View.VISIBLE
                pause = 1
            }
        }
        menu_show.setOnClickListener{
            menu_show.visibility = View.GONE
            pause = 0
        }
        findViewById<ConstraintLayout>(R.id.men).setOnClickListener {  }

        val tmp = this
        findViewById<Button>(R.id.logout).setOnClickListener {
            mGoogleSignInClient?.signOut()
                ?.addOnCompleteListener(this, object : OnCompleteListener<Void?> {
                    override fun onComplete(p0: Task<Void?>) {
                        user = null
                        ingame = 0
                        menu_show.visibility = View.GONE
                        pause = 0
                        Navigation.findNavController(tmp, R.id.fragmentContainerView).navigate(R.id.loginFragment)
                    }
                })
        }

        val switch_sound = findViewById<Switch>(R.id.mute)
        switch_sound.setOnClickListener {
            if(switch_sound.isChecked){
                sound = 1
            } else{
                sound = 0
            }
        }
    }
}