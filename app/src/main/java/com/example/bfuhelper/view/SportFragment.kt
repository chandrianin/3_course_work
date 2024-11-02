package com.example.bfuhelper.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bfuhelper.databinding.FragmentSportBinding
import com.example.bfuhelper.model.sport.SportDataBase
import com.example.bfuhelper.viewModel.SportViewModel

class SportFragment : Fragment() {
    private lateinit var viewModel: SportViewModel
    private var _binding: FragmentSportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSportBinding.inflate(inflater, container, false)
        val view = binding.root

        val application = requireNotNull(this.activity).application
        val dao = SportDataBase.getInstance(application).sportDao

        val viewModelFactory = SportViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[SportViewModel::class.java]

        binding.sportViewModel = viewModel
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}