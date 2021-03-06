package com.example.maccproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.system.exitProcess


class LoginFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun signIn() {
        if(mGoogleSignInClient != null){
            val signInIntent: Intent = mGoogleSignInClient!!.signInIntent
            startActivityForResult(signInIntent, 10)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(user != null){
            exitProcess(0)
        }
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<LinearLayout>(R.id.menu_show)?.visibility = View.GONE
        pause = 0
        activity?.findViewById<ImageView>(R.id.menu)?.visibility = View.GONE
        activity?.findViewById<TextView>(R.id.user_name)?.visibility = View.GONE

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()


        //last signed in
        val acc = this.context?.let { GoogleSignIn.getLastSignedInAccount(it) }
        if(acc != null){
            authCheck(this, acc)
        }
        activity?.findViewById<com.google.android.gms.common.SignInButton>(R.id.sign_in_button)?.visibility = View.VISIBLE

        mGoogleSignInClient = GoogleSignIn.getClient(this.requireContext(), gso)

        activity?.findViewById<com.google.android.gms.common.SignInButton>(R.id.sign_in_button)?.setOnClickListener {
            activity?.findViewById<ProgressBar>(R.id.loading)?.visibility = View.VISIBLE
            activity?.findViewById<com.google.android.gms.common.SignInButton>(R.id.sign_in_button)?.visibility = View.GONE

            GlobalScope.launch(Dispatchers.Main) {
                val c1 = async { signIn() }
                c1.await()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.findViewById<ImageView>(R.id.menu)?.visibility = View.VISIBLE
        activity?.findViewById<TextView>(R.id.user_name)?.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 10) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            if(task.isSuccessful){
                val acc = task.getResult()

                authCheck(this, acc)
                activity?.findViewById<ProgressBar>(R.id.loading)?.visibility = View.GONE
            }
        }
    }

    fun authCheck(frgmt: LoginFragment, acc: GoogleSignInAccount){

        GlobalScope.launch(Dispatchers.IO) {
            var ret:JSONObject? = null
            val c1 = async {
                ret = acc.id?.let { Proxy.getUserData(it) }
            }
            c1.await()
            if(ret != null){
                //l'user ?? presente nel database -> controllo del nickname
                try {
                    user = User(ret!!["id"] as Int, ret!!["name"] as String, ret!!["email"] as String, ret!!["googleId"] as String, ret!!["nickname"] as String?)
                    withContext(Dispatchers.Main){
                        NavHostFragment.findNavController(frgmt).navigate(R.id.startFragment)
                    }
                }catch (e: Exception){
                    user = User(ret!!["id"] as Int, ret!!["name"] as String, ret!!["email"] as String, ret!!["googleId"] as String, null)
                    withContext(Dispatchers.Main){
                        NavHostFragment.findNavController(frgmt).navigate(R.id.nicknameFragment)
                    }
                }
            }
            else{
                //il nuovo utente deve essere aggiunto al database e va aggiunto anche il nickname
                val c2 = async {
                    if(acc.displayName != null && acc.email != null && acc.id != null){
                        ret = Proxy.sendNewUser(acc.displayName!!, acc.email!!, acc.id!!)
                    }
                }
                c2.await()
                //settare l'user come quello appena loggato
                if(ret != null){
                    user = User(ret!!["id"] as Int, ret!!["name"] as String, ret!!["email"] as String, ret!!["googleId"] as String, null)
                    withContext(Dispatchers.Main){
                        NavHostFragment.findNavController(frgmt).navigate(R.id.nicknameFragment)
                    }
                }
            }
        }
    }
}