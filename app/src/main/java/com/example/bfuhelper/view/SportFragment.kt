package com.example.bfuhelper.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bfuhelper.R
import com.example.bfuhelper.databinding.FragmentSportBinding
import com.example.bfuhelper.model.sport.Month
import com.example.bfuhelper.model.sport.SportDataBase
import com.example.bfuhelper.viewModel.SportViewModel

class SportFragment : Fragment() {
    private val tag = "SportFragment"

    private lateinit var viewModel: SportViewModel
    private val sportItemAdapter = SportItemAdapter()
    private var loginDialog: LoginDialogFragment? = null
    private var detailsDialog: DetailsDialogFragment? = null


    private lateinit var monthAdapter: MonthAdapter // Адаптер для месяцев
    private lateinit var monthsLayoutManager: LinearLayoutManager // LayoutManager для месяцев

    private fun setupDaysRecyclerView() {
        binding.sportItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sportItemAdapter
        }
    }

    private fun observeSportItems() {
        viewModel.filteredSportItems.observe(viewLifecycleOwner) { items ->
            items?.let {
                sportItemAdapter.submitList(it)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

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

        val viewModelFactory = SportViewModelFactory(dao, application)
        viewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[SportViewModel::class.java]
        Log.d(tag, "SportFragment ViewModel instance: ${viewModel.hashCode()}")


        setupDaysRecyclerView()
        setupMonthsRecyclerView() // Настраиваем RecyclerView для месяцев
        observeSelectedMonth() // Наблюдаем за выбранным месяцем
        observeAvailableMonths()

        observeSportItems()
        observeToastEvents()
        observeLoadingState()

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
        viewModel.isCredentialsMissing.observe(requireActivity()) { isMissing ->
            Log.d(tag, "Observer (isLoginDataMissing): Value changed to $isMissing")
            if (isMissing) {
                Log.d(
                    tag,
                    "Observer (isLoginDataMissing): Login data IS missing. Attempting to show dialog."
                )
                // Показываем диалог, только если его еще нет
                if (loginDialog == null || loginDialog?.dialog?.isShowing == false) {
                    Log.d(
                        tag,
                        "Observer (isLoginDataMissing): Creating and showing NEW LoginDialogFragment."
                    )
                    loginDialog = LoginDialogFragment.newInstance()
                    loginDialog?.show(childFragmentManager, LoginDialogFragment.TAG)
                } else {
                    Log.d(
                        tag,
                        "Observer (isLoginDataMissing): Login dialog is already active or in process."
                    )

                }
            } else {
                // Если данные для входа НЕ отсутствуют (т.е. успешно вошли), закрываем диалог
                Log.d(
                    tag,
                    "Observer (isLoginDataMissing): Login data is NOT missing. Attempting to dismiss dialog."
                )
                if (loginDialog != null) {
                    // Проверяем, существует ли внутренний диалог и показывается ли он
                    if (loginDialog?.dialog != null && loginDialog?.dialog?.isShowing == true) {
                        Log.d(tag, "Observer (isLoginDataMissing): Dismissing LoginDialogFragment.")
                        loginDialog?.dismiss()
                    } else {
                        Log.d(
                            tag,
                            "Observer (isLoginDataMissing): LoginDialogFragment exists but its internal dialog is null or not showing. No action needed."
                        )
                    }
                    // Важно очистить ссылку после попытки закрытия, чтобы при следующем isMissing=true он был пересоздан
                    loginDialog = null
                } else {
                    Log.d(
                        tag,
                        "Observer (isLoginDataMissing): loginDialog reference is null, no dialog to dismiss."
                    )
                }
            }
        }
        binding.detailsButton.setOnClickListener {
            val detailsDialog = DetailsDialogFragment(
                viewModel.control,
                viewModel.lms,
                viewModel.events,
                viewModel.physical
            )
            detailsDialog.show(childFragmentManager, "DetailsDialogTag")
        }
        return view
    }

    private fun observeAvailableMonths() {
        viewModel.availableMonths.observe(viewLifecycleOwner) { newMonths ->
            monthAdapter.updateMonths(newMonths)
        }
    }

    // Метод для настройки RecyclerView для месяцев
    private fun setupMonthsRecyclerView() {
        monthsLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.monthsRecyclerView.layoutManager = monthsLayoutManager

        monthAdapter = MonthAdapter(Month.entries) { month ->
            viewModel.setSelectedMonth(month)
            // Прокручиваем к выбранному месяцу
            val position = Month.entries.indexOf(month)
            if (position != -1) {
                monthsLayoutManager.scrollToPosition(position)
            }
        }
        binding.monthsRecyclerView.adapter = monthAdapter
    }

    // Метод для наблюдения за выбранным месяцем
    private fun observeSelectedMonth() {
        viewModel.selectedMonth.observe(viewLifecycleOwner) { selectedMonth ->
            monthAdapter.setSelectedMonth(selectedMonth)
            monthAdapter.notifyDataSetChanged()

            // Прокрутка к выбранному месяцу, если он не виден
            val position =
                viewModel.availableMonths.value?.indexOf(selectedMonth) ?: Month.entries.indexOf(
                    selectedMonth
                )
            if (position != -1) {
                scrollToMonthCenter(position)
            }
        }
    }

    // Новый вспомогательный метод для прокрутки к центру
    private fun scrollToMonthCenter(position: Int) {
        // Выполняем прокрутку после того, как макет RecyclerView будет готов.
        // Это гарантирует, что все View-элементы имеют правильные размеры и позиции.
        binding.monthsRecyclerView.post {
            val layoutManager = binding.monthsRecyclerView.layoutManager as? LinearLayoutManager
            if (layoutManager != null) {
                // Находим View-элемент для выбранной позиции.
                val itemCount = monthAdapter.itemCount
                val view = layoutManager.findViewByPosition(position)
                if (view != null) {
                    when (position) {
                        0 -> {
                            // Если это самый первый элемент, прокручиваем к самому началу
                            binding.monthsRecyclerView.smoothScrollBy(
                                -binding.monthsRecyclerView.width,
                                0
                            )
                            Log.d(tag, "scrollToMonthCenter: Scrolled to start position 0.")
                        }

                        itemCount - 1 -> {
                            // Если это самый последний элемент, прокручиваем к самому концу
                            binding.monthsRecyclerView.smoothScrollBy(
                                binding.monthsRecyclerView.width,
                                0
                            )
                            Log.d(
                                tag,
                                "scrollToMonthCenter: Scrolled to end position ${itemCount - 1}."
                            )
                        }

                        else -> {
                            // Вычисляем центр RecyclerView
                            val centerOfRecyclerView = binding.monthsRecyclerView.width / 2
                            // Вычисляем центр дочернего View-элемента
                            val centerOfChild = view.left + view.width / 2
                            // Определяем необходимую величину прокрутки для центрирования
                            val scrollAmount = centerOfChild - centerOfRecyclerView
                            // Плавно прокручиваем RecyclerView на вычисленную величину
                            binding.monthsRecyclerView.smoothScrollBy(scrollAmount, 0)
                            Log.d(
                                tag,
                                "scrollToMonthCenter: Scrolled to center position $position, scrollAmount: $scrollAmount"
                            )
                        }
                    }
                } else {
                    // Резервный вариант, если представление недоступно сразу (например, оно немного за пределами экрана).
                    // Просто прокручиваем до позиции, хотя центрирование может быть неточным,
                    // если представление не видно и его размеры не определены.
                    layoutManager.scrollToPosition(position)
                    Log.d(
                        tag,
                        "scrollToMonthCenter: Fallback: Scrolled to position $position (view not immediately available for centering)."
                    )
                }
            }
        }
    }

    private fun observeToastEvents() {
        viewModel.showToastEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Log.d(tag, "Observer (Toast): Received Toast event. Message: '$message'")
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } ?: run {
                Log.d(
                    tag,
                    "Observer (Toast): Toast event content was null or already handled. No toast shown."
                )
            }
        }
    }

    private fun observeLoadingState() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(tag, "onViewCreated()")


        // Настройка крутилки для обновления данных
        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(
                R.color.scores_primary,
            )

            setProgressBackgroundColorSchemeResource(
                R.color.refresh_indicator
            )

            setOnRefreshListener {
                viewModel.checkLoginData(false)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(tag, "onDestroyView()")
        _binding = null
    }
}