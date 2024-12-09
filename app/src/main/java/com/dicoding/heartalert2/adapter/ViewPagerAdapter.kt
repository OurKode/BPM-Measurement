package com.dicoding.heartalert2.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dicoding.intro.ActivityBpmFragment
import com.dicoding.intro.AgeFragment
import com.dicoding.intro.ChestPainQuestionFragment
import com.dicoding.intro.ChestPainSliderFragment
import com.dicoding.intro.ChestTightnessFragment
import com.dicoding.intro.GenderFragment
import com.dicoding.intro.IntroFragment
import com.dicoding.intro.RestingBpmFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val fragmentList = listOf(
        IntroFragment(),
        GenderFragment(),
        AgeFragment(),
        ChestPainQuestionFragment(),
        ChestPainSliderFragment(),
        RestingBpmFragment(),
        ActivityBpmFragment(),
        ChestTightnessFragment()
        // Add more fragments here for the additional steps
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}