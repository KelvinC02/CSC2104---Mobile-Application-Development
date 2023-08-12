package com.example.recipepal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class BackgroundPagerAdapter : PagerAdapter() {

    private val BACKGROUND_LAYOUTS = arrayOf(
        R.layout.bg_splash_1,
        R.layout.bg_splash_2,
        R.layout.bg_splash_3
    )

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(BACKGROUND_LAYOUTS[position], container, false)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return BACKGROUND_LAYOUTS.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}
