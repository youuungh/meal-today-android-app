package com.example.mealtoday.ui.fragments

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.mealtoday.R
import com.example.mealtoday.adapters.SliderAdapter
import com.example.mealtoday.data.Slider
import com.example.mealtoday.databinding.FragmentMoreBinding
import com.example.mealtoday.viewModel.MoreViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.platform.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderEffectBlur
import kotlin.math.abs

@AndroidEntryPoint
class MoreFragment : Fragment(R.layout.fragment_more) {

    private val moreViewModel: MoreViewModel by viewModels()

    private lateinit var binding: FragmentMoreBinding
    private lateinit var sliderAdapter: SliderAdapter
    private lateinit var dailyAdapter: DailyFragmentAdapter
    private var sliderList: ArrayList<Slider> = ArrayList()
    private val handler = Handler(Looper.myLooper()!!)
    private var currentPagePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sliderAdapter = SliderAdapter()
        dailyAdapter = DailyFragmentAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        enterTransition = MaterialFadeThrough().addTarget(binding.coordinator)
        reenterTransition = MaterialFadeThrough().addTarget(binding.coordinator)
        super.onViewCreated(view, savedInstanceState)

        getSliderMeal()
        setUpTransformer()
        setUpDailyMeal()

//        binding.tabLayout.apply {
//            binding.blurView.setupWith(binding.root, RenderEffectBlur())
//        }
    }

    private fun getSliderMeal() {
        moreViewModel.getSliderMeals()
        moreViewModel.getSliderMealLiveData.observe(viewLifecycleOwner) { data ->
            sliderList = data
            //sliderAdapter = SliderAdapter(sliderList)
            sliderAdapter.differ.submitList(sliderList)
            binding.viewPager.adapter = sliderAdapter
            binding.viewPager.offscreenPageLimit = 3
            binding.viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            binding.viewPager.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    handler.removeCallbacks(sliderRunnable)
                    handler.postDelayed(sliderRunnable, 3000)

                    if (position == sliderList.size - 2) {
                        binding.viewPager.post(runnable)
                    }
                }
            })
            binding.viewPager.setCurrentItem(currentPagePosition, false)
        }
    }

    private fun setUpTransformer() {
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))

        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        binding.viewPager.setPageTransformer(compositePageTransformer)
    }

    private fun setUpDailyMeal() {
        binding.tabViewPager.apply {
            val daysOfWeek = arrayOf("월", "화", "수", "목", "금", "토", "일")
            adapter = dailyAdapter
            TabLayoutMediator(binding.tabLayout, binding.tabViewPager) { tab, position ->
                tab.text = daysOfWeek[position]
            }.attach()
        }
    }

    class DailyFragmentAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 7

        override fun createFragment(position: Int): Fragment {
            return DailyDrinkFragment.newInstance(position)
        }
    }

    private val sliderRunnable = Runnable {
        binding.viewPager.currentItem = binding.viewPager.currentItem + 1
    }

    private val runnable = Runnable {
        sliderList.addAll(sliderList)
        sliderAdapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 3000)
    }
}