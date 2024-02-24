package com.example.mealtoday.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.core.view.doOnPreDraw
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.example.mealtoday.R
import com.example.mealtoday.adapters.SearchAdapter
import com.example.mealtoday.data.Meal
import com.example.mealtoday.databinding.FragmentSearchBinding
import com.example.mealtoday.ui.activities.MainActivity
import com.example.mealtoday.utils.focusAndShowKeyboard
import com.example.mealtoday.viewModel.SearchViewModel
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayList

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private val searchViewModel: SearchViewModel by viewModels()

    private lateinit var binding: FragmentSearchBinding
    private lateinit var navController: NavController
    private lateinit var searchAdapter: SearchAdapter
    private var query: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchAdapter = SearchAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }
        enterTransition = com.google.android.material.transition.platform.MaterialFadeThrough().addTarget(view)
        reenterTransition = com.google.android.material.transition.platform.MaterialFadeThrough().addTarget(view)
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.backButton.setOnClickListener { navController.popBackStack() }

        searchViewModel.clearSearchResult()

        setUpSearchView()
        observeSearchMealData()
        setupSearchRecyclerView()
        clearText()
    }

    private fun setUpSearchView() {
        binding.tvSearch.apply {
            doAfterTextChanged {
                if (!it.isNullOrEmpty())
                    search(it.toString())
                else {
                    TransitionManager.beginDelayedTransition(binding.appBarLayout)
                    binding.clearText.isGone = true
                }
            }
            focusAndShowKeyboard()
        }
    }

    private fun search(query: String) {
        this.query = query
        TransitionManager.beginDelayedTransition(binding.appBarLayout)
        binding.clearText.isVisible = query.isNotEmpty()
        searchViewModel.getSearchMeal(query)
    }

    private fun observeSearchMealData() {
        searchViewModel.getSearchMealLiveData.observe(viewLifecycleOwner) { data ->
            binding.empty.isVisible = data.isEmpty()
            searchAdapter.differ.submitList(data)
        }
    }

    private fun setupSearchRecyclerView() {
        binding.rvSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0){
                        hideKeyboard(view)
                    } else if (dy < 0) {
                        hideKeyboard(view)
                    }
                }
            })
        }
    }

    private fun clearText() {
        binding.clearText.setOnClickListener {
            binding.tvSearch.clearText()
            searchViewModel.clearSearchResult()
        }
    }

    private fun TextInputEditText.clearText() {
        text = null
    }

    private fun hideKeyboard(view: View?) {
        if (view != null) {
            val imm = requireContext().getSystemService<InputMethodManager>()
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onPause() {
        hideKeyboard(view)
        super.onPause()
    }

    override fun onDestroyView() {
        hideKeyboard(view)
        super.onDestroyView()
    }
}