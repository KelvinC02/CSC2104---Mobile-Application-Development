package com.example.recipepal

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import android.widget.ImageView
import android.view.View
import android.widget.Button
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Intent
import androidx.core.view.isVisible

class SplashActivity : AppCompatActivity() {
    private lateinit var startButton: Button
    private lateinit var viewPager: ViewPager
    private lateinit var adapter: BackgroundPagerAdapter
    private lateinit var dot1: ImageView
    private lateinit var dot2: ImageView
    private lateinit var dot3: ImageView

    private val pagerChangeHandler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private val PAGE_DELAY: Long = 3000 // Time in milliseconds for each page change
    private val LAST_PAGE_INDEX = 2

    private var pagerChangeRunnable: Runnable? = null
    private var isLastPagerReached = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        viewPager = findViewById(R.id.viewPager)
        adapter = BackgroundPagerAdapter()
        viewPager.adapter = adapter

        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)

        updateCurrentDot(0) // Set the first dot as selected initially

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                updateCurrentDot(position)
                currentPage = position // Update the current page

                // Cancel the previous automatic paging Runnable if it exists
                pagerChangeRunnable?.let { pagerChangeHandler.removeCallbacks(it) }

                // Reset the timer and trigger the automatic paging again
                if (currentPage == LAST_PAGE_INDEX && !isLastPagerReached) {
                    isLastPagerReached = true
                    showStartButtonWithAnimation()
                } else {
                    hideStartButton()
                    isLastPagerReached = false
                }

                pagerChangeHandler.postDelayed(pagerChangeRunnable!!, PAGE_DELAY)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        startButton = findViewById<Button>(R.id.btn_start)
        startButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Hide the StartButton initially
        hideStartButton()

        // Start automatic paging
        startAutomaticPaging()
    }

    private fun updateCurrentDot(currentDot: Int) {
        dot1.setImageResource(if (currentDot == 0) R.drawable.ic_dot_selected else R.drawable.ic_dot)
        dot2.setImageResource(if (currentDot == 1) R.drawable.ic_dot_selected else R.drawable.ic_dot)
        dot3.setImageResource(if (currentDot == 2) R.drawable.ic_dot_selected else R.drawable.ic_dot)
    }

    private fun showStartButtonWithAnimation() {
        // Load the button animation from XML
        if (!startButton.isVisible) {
            val fadeInAnimation = AnimatorInflater.loadAnimator(this, R.animator.fade_in_button) as AnimatorSet
            fadeInAnimation.setTarget(startButton)
            fadeInAnimation.start()
            startButton.visibility = View.VISIBLE
        }

        // Remove any pending automatic paging callbacks
        pagerChangeHandler.removeCallbacksAndMessages(null)
    }

    private fun hideStartButton() {
        // Hide the StartButton
        startButton.visibility = View.INVISIBLE
    }

    private fun startAutomaticPaging() {
        pagerChangeRunnable = object : Runnable {
            override fun run() {
                if (currentPage < LAST_PAGE_INDEX) {
                    viewPager.setCurrentItem(currentPage + 1, true)
                    pagerChangeHandler.postDelayed(this, PAGE_DELAY)
                } else {
                    // Reached the last page, show the StartButton with animation
                    isLastPagerReached = true
                    showStartButtonWithAnimation()
                }
            }
        }

        // Start automatic paging with the initial delay
        pagerChangeHandler.postDelayed(pagerChangeRunnable!!, PAGE_DELAY)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove any pending callbacks to prevent leaks
        pagerChangeHandler.removeCallbacksAndMessages(null)
    }
}
