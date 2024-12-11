package com.dicoding.heartalert2

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.dicoding.heartalert2.adapter.ViewPagerAdapter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ResultFragment())
                .commit()
        }

        setupIntroViewPager()
    }

    private fun setupIntroViewPager() {
        findViewById<ViewPager2>(R.id.viewPager).apply {
            visibility = View.VISIBLE
            adapter = ViewPagerAdapter(this@MainActivity)
            isUserInputEnabled = false // Disable swiping
        }
        findViewById<FragmentContainerView>(R.id.nav_host_fragment).visibility = View.GONE
    }

    private fun showMainContent() {
        findViewById<ViewPager2>(R.id.viewPager).visibility = View.GONE
        findViewById<FragmentContainerView>(R.id.nav_host_fragment).visibility = View.VISIBLE
    }

    fun moveToNextPage() {
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        if (viewPager.currentItem < viewPager.adapter!!.itemCount - 1) {
            viewPager.currentItem += 1
        } else {
            showMainContent()
        }
    }

    fun moveToPreviousPage() {
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        if (viewPager.currentItem > 0) {
            viewPager.currentItem -= 1
        }
    }

    fun moveToPage(page: Int) {
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        if (page in 0 until viewPager.adapter!!.itemCount) {
            viewPager.currentItem = page
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, ResultFragment())
            .addToBackStack(null)
            .commit()
    }
}