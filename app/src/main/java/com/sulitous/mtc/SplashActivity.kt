package com.sulitous.mtc

import android.content.Intent
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView

class SplashActivity : AppCompatActivity() {

    private var mImageView: ImageView? = null
    private var mText1: TextView? = null
    private var mText2: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mImageView = findViewById(R.id.splash_logo)
        mText1 = findViewById(R.id.splash_text)
        mText2 = findViewById(R.id.splash_text1)
        val animation = AnimationUtils.loadAnimation(this, R.anim.image_anim)
        mImageView!!.startAnimation(animation)
        mText1!!.animation = animation
        mText2!!.animation = animation

        Handler().postDelayed({
            /* Create an Intent that will start the Menu-Activity. */
            val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }, 2000)
    }
}
