package com.example.maccproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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
        val acc = this.context?.let { GoogleSignIn.getLastSignedInAccount(it) }
        if(acc != null){
            activity?.findViewById<ProgressBar>(R.id.loading)?.visibility = View.VISIBLE
            authCheck(this, acc)
            activity?.findViewById<ProgressBar>(R.id.loading)?.visibility = View.GONE
        }

        mGoogleSignInClient = GoogleSignIn.getClient(this.requireContext(), gso)

        activity?.findViewById<com.google.android.gms.common.SignInButton>(R.id.sign_in_button)?.setOnClickListener {
            activity?.findViewById<ProgressBar>(R.id.loading)?.visibility = View.VISIBLE
            activity?.findViewById<com.google.android.gms.common.SignInButton>(R.id.sign_in_button)?.visibility = View.GONE

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

                authCheck(this, acc)
                activity?.findViewById<ProgressBar>(R.id.loading)?.visibility = View.GONE
            }
        }
    }

    fun authCheck(frgmt: LoginFragment, acc: GoogleSignInAccount){
        val scope = CoroutineScope(Dispatchers.IO)
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
                        NavHostFragment.findNavController(frgmt).navigate(R.id.startFragment)
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
                    withContext(Dispatchers.Main){
                        NavHostFragment.findNavController(frgmt).navigate(R.id.nicknameFragment)
                    }
                }
            }
        }
    }
}