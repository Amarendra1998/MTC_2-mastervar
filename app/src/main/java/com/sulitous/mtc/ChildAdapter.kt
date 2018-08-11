package com.sulitous.mtc

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import java.util.*

class ChildAdapter internal constructor(private val mContext: Context, private val mRootRef: FirebaseFirestore, private val i: Int) : RecyclerView.Adapter<ChildAdapter.WaitingView>(), EventListener<DocumentSnapshot> {

    private val mWaitingList: MutableList<Child>
    private val mInflater: LayoutInflater
    private var childType = 0
    private val mUser = FirebaseAuth.getInstance().currentUser
    private val mAuth = FirebaseAuth.getInstance()

    init {
        this.mInflater = LayoutInflater.from(mContext)
        this.mWaitingList = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaitingView {
        val view = mInflater.inflate(R.layout.chlid_list_item, parent, false)
        return WaitingView(mContext, view, mRootRef)
    }

    override fun onBindViewHolder(holder: WaitingView, position: Int) {

            holder.bindToView(mWaitingList[position], i, childType)
            holder.setClickListener(object:RecyclerItemClickListener {
                override fun onClick(view: View,position: Int,isLongClick:Boolean){
            if (i == 0) {
                if (isLongClick) {
                    val child = mWaitingList[position]
                    val gson = Gson()
                    val SChild = gson.toJson(child)
                    val editChildIntent = Intent(mContext, AddChildActivity::class.java)
                    editChildIntent.putExtra("CHILD", SChild)
                    mContext.startActivity(editChildIntent)
                } else {
                    mWaitingList.removeAt(position)
                    notifyItemRemoved(position)
                }
                }
            }
        })

    }

    override fun getItemCount(): Int {
        return mWaitingList.size
    }

    fun clear() {
        mWaitingList.clear()
    }

    fun getChildType(i: Int) {
        this.childType = i
    }

    override fun onEvent(documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e == null) {
            if (documentSnapshot!!.data != null) {
                for (key in documentSnapshot.data!!.keys) {
                    mRootRef.collection("ChildDetails").document(key).get().addOnSuccessListener { documentSnapshot ->
                        val key = documentSnapshot.id
                        val child = documentSnapshot.toObject(Child::class.java)!!
                        child.key = key
                        mWaitingList.add(0, child)

                        notifyDataSetChanged()
                    }.addOnFailureListener { e -> Toast.makeText(mContext, e.localizedMessage, Toast.LENGTH_SHORT).show() }
                }
            }
        } else {
            Toast.makeText(mContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    inner class WaitingView internal constructor(private val mContext: Context, itemView: View, private val mRootRef: FirebaseFirestore) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {
        private val mChildName: TextView
        private val mChildFather: TextView
        private val mChildAge: TextView
        private val mChildPhone: TextView
        private var key: String? = null
        private var recyclerItemClickListener: RecyclerItemClickListener? = null
        private val mSendChildView: ImageView
        private val mRootView: View

        init {
            itemView.setOnLongClickListener(this)
            mChildName = itemView.findViewById(R.id.name)
            mChildFather = itemView.findViewById(R.id.father)
            mChildAge = itemView.findViewById(R.id.age)
            mChildPhone = itemView.findViewById(R.id.phone)
            mSendChildView = itemView.findViewById(R.id.send_child)
            mSendChildView.setOnClickListener { transferToUnderTreatment(mContext) }
            mRootView = itemView.findViewById(R.id.list_layout)
        }

        private fun transferToUnderTreatment(context: Context) {
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setTitle("Are Your Sure")
            alertDialogBuilder
                    .setMessage("Do you want to send this child to MTC")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id ->
                        val sharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.shared_file_user), Context.MODE_PRIVATE)
                        val uid = sharedPreferences.getString(mContext.getString(R.string.uid_key_shared), "")
                        val UID = mUser!!.uid
                        val center = sharedPreferences.getString(mContext.getString(R.string.centre_key_shared), "")
                        val treatmentHashMap = HashMap<String, Any>()
                        treatmentHashMap["treatment"] = 1
                        treatmentHashMap["transferred"] = FieldValue.serverTimestamp()
                        mRootRef.collection("ChildDetails").document(key!!).set(treatmentHashMap, SetOptions.merge())
                        val pushHashMap = HashMap<String, Any>()
                        pushHashMap.put(key!!, FieldValue.serverTimestamp())
                        mRootRef.collection("Treatment").document(UID).set(pushHashMap, SetOptions.merge())
                        mRootRef.collection("UnderTreatment").document(UID).set(pushHashMap, SetOptions.merge())
                        val deleteHashMap = HashMap<String, Any>()
                        deleteHashMap.put(key!!,FieldValue.delete())
                        mRootRef.collection("WaitingList").document(UID).update(deleteHashMap)
                        recyclerItemClickListener!!.onClick(mSendChildView, adapterPosition, false)
                    }
                    .setNegativeButton("No") { dialog, id -> dialog.cancel() }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        internal fun bindToView(child: Child, i: Int, childType: Int) {
            key = child.key
            mChildName.text = child.name
            mChildFather.text = child.father
            mChildAge.text = child.age.toString()
            mChildPhone.text = child.phone
            if (i == 0) {
                mSendChildView.visibility = View.GONE
            } else if (i == 1) {
                mSendChildView.visibility = View.GONE
                if (child.treatment == 1) {
                    mSendChildView.visibility = View.GONE
                    mRootView.setBackgroundResource(R.drawable.red)
                } else if (child.treatment == 2) {
                    mSendChildView.visibility = View.GONE
                    mRootView.setBackgroundResource(R.drawable.yellow)
                } else if (child.treatment == 3) {
                    mSendChildView.visibility = View.VISIBLE
                    mRootView.setBackgroundResource(R.drawable.green)
                }
            }
            viewList(child, childType)
        }

        private fun viewList(child: Child, childType: Int) {
            when (childType) {
                0 -> mRootView.visibility = View.VISIBLE
                1 -> if (child.treatment != 1) {
                    mRootView.visibility = View.GONE
                }
                2 -> if (child.treatment != 2) {
                    mRootView.visibility = View.GONE
                }
                3 -> if (child.treatment != 3) {
                    mRootView.visibility = View.VISIBLE
                }
            }
        }

        internal fun setClickListener(recyclerItemClickListener: RecyclerItemClickListener) {
            this.recyclerItemClickListener = recyclerItemClickListener
        }

        override fun onLongClick(v: View): Boolean {
            recyclerItemClickListener!!.onClick(v, adapterPosition, true)
            return true
        }
    }
}
