package app.bughunt.medhealthcare

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import app.bughunt.medhealthcare.base.BaseActivity
import app.bughunt.medhealthcare.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : BaseActivity() {


    companion object {

        private const val RC_SIGN_IN = 1100
    }

    private var messageReference: DatabaseReference? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    private var btnGoogle: SignInButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupFirebase()
        setupFirebaseDatabase()
        setupView()
    }


    private fun setupView() {

        btnGoogle = findViewById(R.id.btn_sign_in_with_google)
        btnGoogle?.setOnClickListener({

            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        })
    }

    private fun setupFirebase() {

        mAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            // hideProgressDialog();
            if (user != null) {
                // User is signed in
                goToMainActivity()
            } else {
                setupGoogleSign()
            }
        }
    }

    private fun setupFirebaseDatabase() {
        val database = FirebaseDatabase.getInstance()
        messageReference = database.getReference("user")
    }

    private fun setupGoogleSign() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this) { }
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    override fun onStart() {
        super.onStart()

        mGoogleApiClient?.connect()
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    override fun onStop() {
        super.onStop()

        if (mGoogleApiClient != null && mGoogleApiClient?.isConnected!!) {
            mGoogleApiClient?.stopAutoManage(LoginActivity@ this)
            mGoogleApiClient?.disconnect()
        }
        mAuth!!.removeAuthStateListener(mAuthListener!!)
    }


    private fun goToMainActivity() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    private fun connectGoogle(result: GoogleSignInResult) {
        showProgressDialog();
        //  Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.

            val acct = result.signInAccount
            firebaseAuthWithGoogle(acct!!)
        } else {
            hideProgressDialog()
            Log.d("Test", "handleSignInResult:" + result.isSuccess + " " + result.status);
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        hideProgressDialog()
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->

                    if (!task.isSuccessful) {
                        Log.w("Firebase", "signInWithCredential", task.exception);

                    } else {
                        createAccountToRealTimeDB()
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Activity.RESULT_CANCELED && resultCode == Activity.RESULT_OK) {

            finish()

        } else if (requestCode == RC_SIGN_IN) { // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            connectGoogle(result)

        }
    }

    fun createAccountToRealTimeDB() {

        val mUser = FirebaseAuth.getInstance().currentUser
        if (mUser != null) {

            val myUser = User(mUser.email, mUser.displayName, mUser.photoUrl.toString())
            messageReference?.child(mUser.uid)?.setValue(myUser)

        }
    }

}

