package com.sulitous.mtc

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.support.v7.widget.Toolbar
import android.view.View

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ProfileActivity : AppCompatActivity() {

    private var mUser: FirebaseUser? = null
    private var centre: String? = null
    private val phone: String? = null
    private var mUnderTreatmentCount: TextView? = null
    private var mWaitingListCount: TextView? = null
    private var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        toolbar = findViewById<View>(R.id.myappbar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("Profile")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val mLsName = findViewById<TextView>(R.id.ls_name)
        val mLsEmail = findViewById<TextView>(R.id.ls_email)
        val mLsPhone = findViewById<TextView>(R.id.ls_phone)
        val mLsCentre = findViewById<TextView>(R.id.ls_centre)
        mUnderTreatmentCount = findViewById(R.id.total_under_treatment)
        mWaitingListCount = findViewById(R.id.total_waiting_list)

        mUser = FirebaseAuth.getInstance().currentUser
        val sharedPreferences = getSharedPreferences(getString(R.string.shared_file_user), Context.MODE_PRIVATE)
        centre = sharedPreferences.getString(getString(R.string.centre_key_shared), "")
        //        phone = sharedPreferences.getString(getString(R.string.phone),"");

        mLsName.text = mUser!!.displayName
        mLsEmail.text = mUser!!.email
        mLsPhone.text = "7023211995"
        mLsCentre.text = centre
    }
}
