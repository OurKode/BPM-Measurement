package com.dicoding.intro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.dicoding.heartalert2.MainActivity
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.adapter.ViewPagerAdapter

class IntroActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = ViewPagerAdapter(this)
        viewPager.isUserInputEnabled = false // Disable swiping
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currentItem = viewPager.currentItem
        outState.putInt("currentItem", currentItem)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val currentItem = savedInstanceState.getInt("currentItem", 0)
        viewPager.currentItem = currentItem
    }

    fun moveToNextPage() {
        if (viewPager.currentItem < viewPager.adapter!!.itemCount - 1) {
            viewPager.currentItem = viewPager.currentItem + 1
        } else {
            // Save that the user has completed the intro
            val sharedPreferences = getSharedPreferences("HeartAlertPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstTime", false)
            editor.apply()

            // Finish the IntroActivity and Start the MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun moveToPreviousPage() {
        val sharedPreferences = getSharedPreferences("HeartAlertPrefs", Context.MODE_PRIVATE)
        val skipChestPainSlider = sharedPreferences.getBoolean("skipChestPainSlider", false)

        if (viewPager.currentItem == 5 && skipChestPainSlider) {
            viewPager.currentItem = 3 // Navigate back to chest pain question fragment
        } else if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    fun moveToPage(page: Int){
        if (page in 0 until viewPager.adapter!!.itemCount) {
            viewPager.currentItem = page
        }
    }
}