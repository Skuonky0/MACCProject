package com.example.maccproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*
import org.json.JSONObject


class LoginFragment : Fragment() {

    lateinit var mGoogleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, 10)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        //last signed in
        /*val account = this.context?.let { GoogleSignIn.getLastSignedInAccount(it) }
        if(account != null){
            Log.i("account", account.email+" "+account.displayName+" "+account.id)
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val c1 = async {
                    val ret = account.id?.let { Proxy.getUserData(it) }
                }
                c1.await()
            }
            NavHostFragment.findNavController(this).navigate(R.id.gameFragment)
        }*/

        mGoogleSignInClient = GoogleSignIn.getClient(this.requireContext(), gso)

        activity?.findViewById<com.google.android.gms.common.SignInButton>(R.id.sign_in_button)?.setOnClickListener {
            activity?.findViewById<TextView>(R.id.loading)?.visibility = View.VISIBLE
            activity?.findViewById<com.google.android.gms.common.SignInButton>(R.id.sign_in_button)?.visibility = View.GONE

            /*val scope = CoroutineScope(Dispatchers.Main)
            val frgmt = this
            scope.launch {
                var ret : JSONObject? = null
                withContext(Dispatchers.IO) {
                    val c1 = async {
                        ret = Proxy.getUserData("0")
                    }
                    c1.await()
                }
                Log.i("User", ret.toString())
                activity?.findViewById<TextView>(R.id.loading)?.visibility = View.GONE
                //NavHostFragment.findNavController(frgmt).navigate(R.id.gameFragment)//da togliere per il log in
            }*/
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
                val c1 = async { signIn() }
                c1.await()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 10) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            if(task.isSuccessful){
                val acc = task.getResult()
                Log.i("account", acc.email+" "+acc.displayName+" "+acc.id)
                //da creare la parte dove si controlla se l'user è esistente nel database e in caso si crea
                val scope = CoroutineScope(Dispatchers.IO)
                val frgmt = this
                scope.launch {
                    var ret:JSONObject? = null
                    val c1 = async {
                        ret = acc.id?.let { Proxy.getUserData(it) }
                    }
                    c1.await()
                    if(ret != null){
                        //l'user è presente nel database -> controllo del nickname
                        try {
                            user = User(ret!!["name"] as String, ret!!["email"] as String, ret!!["googleId"] as String, ret!!["nickname"] as String?)
                            withContext(Dispatchers.Main){
                                NavHostFragment.findNavController(frgmt).navigate(R.id.gameFragment)
                            }
                        }catch (e: Exception){
                            user = User(ret!!["name"] as String, ret!!["email"] as String, ret!!["googleId"] as String, null)
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
                            user = User(ret!!["name"] as String, ret!!["email"] as String, ret!!["googleId"] as String, null)
                        }
                    }
                }

                activity?.findViewById<TextView>(R.id.loading)?.visibility = View.GONE
                //naviga al gioco
                NavHostFragment.findNavController(this).navigate(R.id.gameFragment)
            }
        }
    }
}