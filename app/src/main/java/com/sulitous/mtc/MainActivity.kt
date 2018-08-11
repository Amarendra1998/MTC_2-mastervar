package com.sulitous.mtc

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var mUser: FirebaseUser? = null
    private var mRootRef: FirebaseFirestore? = null
    private var mChildAdapter: ChildAdapter? = null
    private var registration: ListenerRegistration? = null
    private var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //TODO 4 add Offline support
        toolbar = findViewById<View>(R.id.myappbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "MTC"
        mAuth = FirebaseAuth.getInstance()
        authWithFireBase()
        mRootRef = FirebaseFirestore.getInstance()
        findViewById<View>(R.id.add_child_fab).setOnClickListener { switchToAddChildActivity() }

        val waitingList = findViewById<RecyclerView>(R.id.waiting_list)
        val manager = LinearLayoutManager(this@MainActivity)
        manager.orientation = LinearLayoutManager.VERTICAL
        waitingList.layoutManager = manager
        mChildAdapter = ChildAdapter(this@MainActivity, mRootRef!!, 0)
        waitingList.adapter = mChildAdapter
    }

    private fun switchToAddChildActivity() {
        val addChildIntent = Intent(this@MainActivity, AddChildActivity::class.java)
        startActivity(addChildIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_logout) {
            mAuth!!.signOut()
            return true
        } else if (id == R.id.action_underTreatment) {
            val underTreatmentIntent = Intent(this@MainActivity, UnderTreatmentActivity::class.java)
            startActivity(underTreatmentIntent)
            return true
        } else if (id == R.id.action_profile) {
            val profileIntent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(profileIntent)
        } else if (id == R.id.action_feedback) {
            val feedbackIntent = Intent(this@MainActivity, FeedbackActivity::class.java)
            startActivity(feedbackIntent)
            return true
        }
        return false
    }

    public override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    override fun onResume() {
        super.onResume()
        if (mUser != null) {
            if (registration == null) {
                mChildAdapter!!.clear()
                registration = mRootRef!!.collection("WaitingList").document(mUser!!.uid).addSnapshotListener(mChildAdapter!!)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (registration != null) {
            mChildAdapter!!.clear()
            registration!!.remove()
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (mUser != null) {
            if (registration == null) {
                mChildAdapter!!.clear()
                registration = mRootRef!!.collection("WaitingList").document(mUser!!.uid).addSnapshotListener(mChildAdapter!!)
            }
        }
    }

    public override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth!!.removeAuthStateListener(mAuthListener!!)
        }
        if (registration != null) {
            mChildAdapter!!.clear()
            registration!!.remove()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mAuthListener != null) {
            mAuth!!.removeAuthStateListener(mAuthListener!!)
        }
        if (registration != null) {
            registration!!.remove()
        }
    }

    private fun authWithFireBase() {
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            mUser = firebaseAuth.currentUser
            if (mUser == null) {
                switchToLoginActivity()
            } else {
                mChildAdapter!!.clear()
                if (registration == null) {
                    mChildAdapter!!.clear()
                    registration = mRootRef!!.collection("WaitingList").document(mUser!!.uid).addSnapshotListener(mChildAdapter!!)
                }
            }
        }
    }

    private fun switchToLoginActivity() {
        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }
}
