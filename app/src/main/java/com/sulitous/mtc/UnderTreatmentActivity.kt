package com.sulitous.mtc

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import android.support.v7.widget.Toolbar

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UnderTreatmentActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {

    private var mChildAdapter: ChildAdapter? = null
    private var registration: ListenerRegistration? = null
    private var mRootRef: FirebaseFirestore? = null
    private var childType = 0
    private var mUser: FirebaseUser? = null
    private var mAuth: FirebaseAuth? = null
    private val mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_under_treatment)
        toolbar = findViewById<View>(R.id.myappbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "UnderTreatment"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mAuth = FirebaseAuth.getInstance()
        mUser = FirebaseAuth.getInstance().currentUser
        mRootRef = FirebaseFirestore.getInstance()
        val mRadioGroup = findViewById<RadioGroup>(R.id.radio_childrenType)
        val waitingList = findViewById<RecyclerView>(R.id.under_treatment_list)
        val manager = LinearLayoutManager(this@UnderTreatmentActivity)
        manager.orientation = LinearLayoutManager.VERTICAL
        waitingList.layoutManager = manager
        mChildAdapter = ChildAdapter(this@UnderTreatmentActivity, mRootRef!!, 1)
        mChildAdapter!!.getChildType(childType)
        waitingList.adapter = mChildAdapter
        mRadioGroup.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(radioGroup: RadioGroup, i: Int) {
        if (i == R.id.radio_un_approved) {
            childType = 1
            refreshData()
        } else if (i == R.id.radio_under_treatment) {
            childType = 2
            refreshData()
        } else if (i == R.id.radio_treated) {
            childType = 3
            refreshData()
        }
    }

    private fun refreshData() {
        if (registration != null) {
            mChildAdapter!!.clear()
            registration!!.remove()
            mChildAdapter!!.getChildType(childType)
            /* SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_file_user), Context.MODE_PRIVATE);
            String UID = sharedPreferences.getString(getString(R.string.uid_key_shared),"");*/
            val uid = mUser!!.uid
            registration = mRootRef!!.collection("Treatment").document(uid).addSnapshotListener(mChildAdapter!!)

        }
    }

    override fun onResume() {
        super.onResume()
        if (mUser != null) {
            if (registration == null) {
                mChildAdapter!!.clear()

                /*  SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_file_user), Context.MODE_PRIVATE);
            String UID = sharedPreferences.getString(getString(R.string.uid_key_shared),"");*/
                val uid = mUser!!.uid
                registration = mRootRef!!.collection("Treatment").document(uid).addSnapshotListener(mChildAdapter!!)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mUser != null) {
            if (registration != null) {
                mChildAdapter!!.clear()
                registration!!.remove()
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (mUser != null) {
            if (registration == null) {
                mChildAdapter!!.clear()
                /* SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_file_user), Context.MODE_PRIVATE);
            String UID = sharedPreferences.getString(getString(R.string.uid_key_shared),"");*/
                val uid = mUser!!.uid
                registration = mRootRef!!.collection("Treatment").document(uid).addSnapshotListener(mChildAdapter!!)
            }
        }
    }

    public override fun onStop() {
        super.onStop()

        if (registration != null) {
            mChildAdapter!!.clear()
            registration!!.remove()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (registration != null) {
            registration!!.remove()
}
}
}
