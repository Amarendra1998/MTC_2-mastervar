package com.sulitous.mtc

import android.view.View

internal interface RecyclerItemClickListener {

    fun onClick(view: View, position: Int, isLongClick: Boolean)

}