package com.example.mealtoday.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealtoday.R
import com.example.mealtoday.adapters.CategoryAdapter
import com.example.mealtoday.databinding.FragmentCategoryBinding
import com.example.mealtoday.ui.activities.MainActivity
import com.example.mealtoday.viewModel.CategoryViewModel
import com.google.android.material.transition.platform.MaterialElevationScale
import com.google.android.material.transition.platform.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryFragment : Fragment(R.layout.fragment_category) {

    private val categoryViewModel: CategoryViewModel by viewModels()
    private val args: CategoryFragmentArgs by navArgs()

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var navController: NavController
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryAdapter = CategoryAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).addTarget(binding.root)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).addTarget(binding.root)
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        binding.backButton.setOnClickListener { navController.popBackStack() }
        binding.categoryName.text = args.categoryName

        getCategoryInfo()
        setUpCategoryRecyclerView()
    }

    private fun getCategoryInfo() {
        lifecycleScope.launch {
            categoryViewModel.getCategory(args.categoryName)
            categoryViewModel.categoryStateFlow.collect { data ->
                categoryAdapter.differ.submitList(data)
            }
        }
    }

    private fun setUpCategoryRecyclerView() {
        binding.rvCategoryContent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }
}