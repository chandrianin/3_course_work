package com.example.bfuhelper.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bfuhelper.R
import com.example.bfuhelper.databinding.FragmentSportBinding
import com.example.bfuhelper.model.sport.SportDataBase
import com.example.bfuhelper.viewModel.SportViewModel

class SportFragment : Fragment() {
    private lateinit var viewModel: SportViewModel
    private var _binding: FragmentSportBinding? = null
    private val binding get() = _binding!!

    private lateinit var scoresProgressView: CircularProgressView
    private lateinit var scoresTextView: TextView
    private lateinit var comingsTextView: TextView


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
        binding.lifecycleOwner = viewLifecycleOwner

        scoresProgressView = binding.scoresProgressView
        scoresTextView = binding.scoresTextView
        comingsTextView = binding.comingsTextView

        // Observer'ы для баллов
        viewModel.scores.observe(viewLifecycleOwner) { currentScores ->
            val maxScores = viewModel.maxScores.value ?: 0
            scoresTextView.text = "$currentScores/$maxScores"
        }

        viewModel.maxScores.observe(viewLifecycleOwner) { maxScores ->
            val currentScores = viewModel.scores.value ?: 0
            scoresTextView.text = "$currentScores/$maxScores"
        }

        // Observer'ы для посещений
        viewModel.visits.observe(viewLifecycleOwner) { currentVisits ->
            val maxVisits = viewModel.maxVisits.value ?: 0
            comingsTextView.text = "$currentVisits/$maxVisits"
        }

        viewModel.maxVisits.observe(viewLifecycleOwner) { maxVisits ->
            val currentVisits = viewModel.visits.value ?: 0
            comingsTextView.text = "$currentVisits/$maxVisits"
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка крутилки для обновления данных
        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(
                R.color.scores_primary,
                R.color.scores_transparent,
            )

            setProgressBackgroundColorSchemeResource(
                R.color.refresh_indicator
            )

            setOnRefreshListener {
                // Обновление данных
                // Например: viewModel.refreshData()
                // Когда данные загрузятся:
                // binding.swipeRefreshLayout.isRefreshing = false
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}