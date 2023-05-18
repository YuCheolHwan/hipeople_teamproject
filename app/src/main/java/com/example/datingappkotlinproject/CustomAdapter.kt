package com.example.datingappkotlinproject

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

// fragment 연결 어댑터
class CustomAdapter (activity: FragmentActivity) : FragmentStateAdapter(activity) {
    val fragmentList = ArrayList<Fragment>()
    override fun getItemCount(): Int = fragmentList.size
    override fun createFragment(position: Int): Fragment = fragmentList.get(position)

    fun addListFragment(fragment: Fragment) {
        this.fragmentList.add(fragment)
    }
}