package com.sulitous.mtc

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Switch
import android.widget.Toast
import android.support.v7.widget.Toolbar

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.Locale

class FeedbackActivity : AppCompatActivity() {

    private var mPaymentView: Switch? = null
    private var mDietView: RatingBar? = null
    private var mAnmView: EditText? = null
    private var mMtcView: EditText? = null
    private var mRootRef: FirebaseFirestore? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null
    private var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        mPaymentView = findViewById(R.id.feedback_payment)
        mDietView = findViewById(R.id.feedback_diet_rating)
        mAnmView = findViewById(R.id.feedback_anm)
        mMtcView = findViewById(R.id.feedback_mtc)
        toolbar = findViewById<View>(R.id.myappbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Feadback"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val mFeedbackSubmitView = findViewById<Button>(R.id.feedback_submit)
        mFeedbackSubmitView.setOnClickListener { checkData() }
        mRootRef = FirebaseFirestore.getInstance()
    }

    private fun checkData() {
        val payment = mPaymentView!!.isChecked
        val diet = mDietView!!.numStars.toDouble()
        var anm = mAnmView!!.text.toString().trim { it <= ' ' }
        var mtc = mMtcView!!.text.toString().trim { it <= ' ' }


        if (TextUtils.isEmpty(anm)) {
            anm = "Nothing"

        }

        if (TextUtils.isEmpty(mtc)) {
            mtc = "Nothing"
        }

        mAnmView!!.setText("")
        mMtcView!!.setText("")
        onSubmit(payment, diet, anm, mtc)
    }

    private fun onSubmit(payment: Boolean, diet: Double, anm: String, mtc: String) {
        val showDialog = ShowDialog(this@FeedbackActivity)
        showDialog.setTitle("Feedback")
        showDialog.setMessage("Uploading Feedback")
        showDialog.show()
        val feedbackHashMap = HashMap<String, Any>()
        feedbackHashMap["payment"] = payment
        feedbackHashMap["diet"] = diet
        feedbackHashMap["anm"] = anm
        feedbackHashMap["mtc"] = mtc
        feedbackHashMap["timeStamp"] = FieldValue.serverTimestamp()

        val user = FirebaseAuth.getInstance().currentUser!!
// TODO add user display name
        feedbackHashMap["LS"] = "Gourav Karwasara"
        val sharedPreferences = getSharedPreferences(getString(R.string.shared_file_user), Context.MODE_PRIVATE)
        val centre = sharedPreferences.getString(getString(R.string.centre_key_shared), "")
        val uid = firebaseUser!!.uid
        val timeStamp = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        mRootRef!!.collection("Feedback").document(uid).collection(timeStamp).add(feedbackHashMap).addOnSuccessListener {
            showDialog.cancel()
            Toast.makeText(this@FeedbackActivity, "Successfully Uploaded", Toast.LENGTH_SHORT).show()
            val mine = Intent(this@FeedbackActivity, MainActivity::class.java)
            startActivity(mine)
            finish()
        }.addOnFailureListener { e ->
            showDialog.cancel()
            Toast.makeText(this@FeedbackActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }
}
