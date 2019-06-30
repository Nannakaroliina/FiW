package com.doubleshadow.fiw

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    // Declaring Auth
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Button listeners
        login_btn.setOnClickListener(this)
        create_account_btn.setOnClickListener(this)
        logout_btn.setOnClickListener(this)
        verify_email_btn.setOnClickListener(this)
        open_map_btn.setOnClickListener(this)
        add_location_btn.setOnClickListener(this)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

    }

    // Check user on start
    public override fun onStart() {
        super.onStart()

        // Chek if user is signed in and update UI
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }

        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // update UI for current User
                    val user = mAuth!!.getCurrentUser()
                    updateUI(user)
                } else {
                    // Sign in fail
                    Log.e(TAG, "signIn: Fail!", task.exception)
                    updateUI(null)
                }

            }

    }

    // Create new user with email
    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm()) {
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // If creating account success, update ui with new user's data
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = mAuth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

            }
    }

    private fun logOut() {
        mAuth.signOut()
        updateUI(null)
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = login_email.text.toString()
        if (TextUtils.isEmpty(email)) {
            login_email.error = "Required."
            valid = false
        } else {
            login_email.error = null
        }

        val password = login_password.text.toString()
        if (TextUtils.isEmpty(password)) {
            login_password.error = "Required."
            valid = false
        } else {
            login_password.error = null
        }

        return valid
    }

    private fun sendEmailVerification() {
        // Disable button
        verify_email_btn.isEnabled = false

        // Send verification email to user
        val user = mAuth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                // Re-enable button
                verify_email_btn.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(baseContext,
                        "Verification email sent to ${user.email} ",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "sendEmailVerification", task.exception)
                    Toast.makeText(baseContext,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }

            }

    }

    private fun updateUI(user: FirebaseUser?) {

        if (user != null) {
            status.text = getString(R.string.emailpassword_status_fmt,
                user.email, user.isEmailVerified)
            detail.text = getString(R.string.firebase_status_fmt, user.uid)

            welcome_Text.visibility = View.VISIBLE
            title_Text.visibility = View.GONE
            login_create_buttons.visibility = View.GONE
            emailPasswordFields.visibility = View.GONE
            signedInButtons.visibility = View.VISIBLE

            verify_email_btn.isEnabled = !user.isEmailVerified
        } else {
            status.setText(R.string.signed_out)
            detail.text = null

            welcome_Text.visibility = View.GONE
            title_Text.visibility = View.VISIBLE
            login_create_buttons.visibility = View.VISIBLE
            emailPasswordFields.visibility = View.VISIBLE
            signedInButtons.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.create_account_btn -> createAccount(login_email.text.toString(), login_password.text.toString())
            R.id.login_btn -> signIn(login_email.text.toString(), login_password.text.toString())
            R.id.logout_btn -> logOut()
            R.id.verify_email_btn -> sendEmailVerification()
            R.id.open_map_btn -> openMap()
            R.id.add_location_btn -> addLocationActivity()
        }
    }

    private fun addLocationActivity() {
        val intent = Intent(this, SaveActivity::class.java)
        startActivity(intent)
    }

    private fun openMap() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "EmailPassword"

    }

}
