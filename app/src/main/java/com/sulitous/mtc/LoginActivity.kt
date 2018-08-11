package com.sulitous.mtc

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

import java.util.regex.Matcher
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private var mUserEmailView: EditText? = null
    private var mUserPassView: EditText? = null
    private var mUserEmailLayout: TextInputLayout? = null
    private var mUserPassLayout: TextInputLayout? = null
    private var mAuth: FirebaseAuth? = null
    private var mRootRef: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val inflater = LayoutInflater.from(this)
        mUserEmailView = findViewById(R.id.login_email)
        mUserPassView = findViewById(R.id.login_pass)
        mUserEmailLayout = findViewById(R.id.login_email_layout)
        mUserPassLayout = findViewById(R.id.login_pass_layout)
        val forgotView = findViewById<TextView>(R.id.forgot_password)
        val mLoginButton = findViewById<Button>(R.id.login_button)
        mRootRef = FirebaseFirestore.getInstance()

        mLoginButton.setOnClickListener { checkData() }
        mAuth = FirebaseAuth.getInstance()
        if (mAuth!!.currentUser != null) {
            switchToMainActivity()
        }
        forgotView.setOnClickListener { showEmailDialog() }
    }

    private fun showEmailDialog() {
        val dialog = Dialog(this@LoginActivity)
        dialog.setContentView(R.layout.email_dialog)
        val sendLink = dialog.findViewById<Button>(R.id.email_link_button)
        val emailLayout = dialog.findViewById<TextInputLayout>(R.id.email_link_layout)
        val emailView = dialog.findViewById<TextInputEditText>(R.id.email_link_view)

        sendLink.setOnClickListener {
            val email = emailView.text.toString()

            if (TextUtils.isEmpty(email)) {
                emailLayout.error = getString(R.string.field_required)
            } else if (isEmailValid(email)) {
                emailLayout.error = getString(R.string.invalid_email)
            } else {
                emailLayout.error = null
            }

            if (emailLayout.error == null) {
                hideKeyboard()
                val actionCodeSettings = ActionCodeSettings.newBuilder()
                        // URL you want to redirect back to. The domain (www.example.com) for this
                        // URL must be whitelisted in the Firebase Console.
                        .setUrl("https://www.towaso.com/")
                        // This must be true
                        .setHandleCodeInApp(true)
                        .setAndroidPackageName(
                                "com.sulitous.mtc",
                                true, /* installIfNotAvailable */
                                16.toString()    /* minimumVersion */)
                        .build()

                val auth = FirebaseAuth.getInstance()
                auth.sendSignInLinkToEmail(email, actionCodeSettings).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        switchToMainActivity()
                    }
                }
            } else {
                Toast.makeText(this@LoginActivity, R.string.invalid_email, Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun switchToMainActivity() {
        val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }

    private fun checkData() {
        mUserEmailView!!.error = null
        mUserPassView!!.error = null

        val password = mUserPassView!!.text.toString()
        val email = mUserEmailView!!.text.toString()

        if (TextUtils.isEmpty(password)) {
            mUserPassLayout!!.error = getString(R.string.field_required)
        } else if (!isPasswordValid(password)) {
            mUserPassLayout!!.error = getString(R.string.invalid_password)
        } else {
            mUserPassLayout!!.error = null
        }

        if (TextUtils.isEmpty(email)) {
            mUserEmailLayout!!.error = getString(R.string.field_required)
        } else if (isEmailValid(email)) {
            mUserEmailLayout!!.error = getString(R.string.invalid_email)
        } else {
            mUserEmailLayout!!.error = null
        }

        if (mUserEmailLayout!!.error == null && mUserPassLayout!!.error == null) {
            hideKeyboard()
            login(email, password)
        } else {
            Toast.makeText(this, R.string.login_input_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun login(email: String, password: String) {
        val showDialog = ShowDialog(this@LoginActivity)
        showDialog.setTitle("Logging")
        showDialog.setMessage("Checking credits")
        showDialog.show()
        mAuth!!.signInWithEmailAndPassword(email, password).addOnSuccessListener { authResult ->
            mRootRef!!.collection("LS").document(authResult.user.uid).get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    val ls = documentSnapshot.toObject(LS::class.java)
                    val sharedPreferences = getSharedPreferences(getString(R.string.shared_file_user), Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString(getString(R.string.name_key_shared), ls!!.name)
                    editor.putString(getString(R.string.state_key_shared), ls.state)
                    editor.putString(getString(R.string.district_key_shared), ls.district)
                    editor.putString(getString(R.string.centre_key_shared), ls.centre)
                    editor.putString(getString(R.string.block_key_shared), ls.block)
                    editor.putString(getString(R.string.address_key_shared), ls.address)
                    editor.putString(getString(R.string.uid_key_shared), authResult.user.uid)
                    editor.apply()
                    showDialog.cancel()
                    switchToMainActivity()
                } else {
                    mAuth!!.signOut()
                    showDialog.cancel()
                    Toast.makeText(this@LoginActivity, R.string.not_ls, Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                mAuth!!.signOut()
                showDialog.cancel()
                Toast.makeText(this@LoginActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            showDialog.cancel()
            Toast.makeText(this@LoginActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mUserEmailView!!.windowToken, 0)
    }

    private fun isEmailValid(email: String): Boolean {
        val pattern = Pattern.compile("\\w+([-+.]\\w+)*" + "@"
                + "\\w+([-.]\\w+)*" + "\\." + "\\w+([-.]\\w+)*")
        val matcher = pattern.matcher(email)
        return !matcher.matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
}
