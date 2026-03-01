package com.example.bfuhelper.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.bfuhelper.R
import com.example.bfuhelper.model.sport.SportDataBase
import com.example.bfuhelper.viewModel.SportViewModel

class DetailsDialogFragment(
    private val control: Int = 0,
    private val lms: Int = 0,
    private val events: Int = 0,
    private val physical: Int = 0
) :
    DialogFragment() {

    // Объявляем ViewModel, чтобы получить доступ к ней
    private lateinit var sportViewModel: SportViewModel

    companion object {
        const val TAG = "DetailsDialogFragment"

        fun newInstance(): DetailsDialogFragment {
            return DetailsDialogFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Получаем экземпляр ViewModel, scoped к Activity
        // Это гарантирует, что диалог использует тот же ViewModel, что и SportFragment
        val application = requireNotNull(this.activity).application
        val dao = SportDataBase.getInstance(application).sportDao
        // AuthRepository здесь не создаем, он создается внутри SportViewModelFactory

        val viewModelFactory = SportViewModelFactory(dao, application) // Создаем фабрику

        sportViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[SportViewModel::class.java]
        Log.d(TAG, "DetailsDialogFragment ViewModel instance: ${sportViewModel.hashCode()}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Устанавливаем макет
        return inflater.inflate(R.layout.dialog_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем внешний вид диалога (прозрачный фон, чтобы был виден rounded_dialog_background)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.findViewById<TextView>(R.id.details_control_value).text = control.toString()
        view.findViewById<TextView>(R.id.details_lms_value).text = lms.toString()
        view.findViewById<TextView>(R.id.details_sport_events_value).text = events.toString()
        view.findViewById<TextView>(R.id.details_physical_value).text = physical.toString()

//        // Инициализация элементов UI
//        usernameEditText = view.findViewById(R.id.username_edit_text)
//        passwordEditText = view.findViewById(R.id.password_edit_text)
//        loginButton = view.findViewById(R.id.login_button)
//
//        // Кнопка "Войти"
//        loginButton.setOnClickListener {
//            val username = usernameEditText.text.toString()
//            val password = passwordEditText.text.toString()
//            Log.d(tag, "Нажата 'Войти': $username, $password")
//
//            if (username.isNotEmpty() && password.isNotEmpty()) {
//
//                sportViewModel.saveAndLogin(username, password)
//                // Диалог будет автоматически закрыт, когда ViewModel обновит isLoginDataMissing на false
//                // в SportFragment, который наблюдает за ним.
//            } else {
//                Toast.makeText(context, "Пожалуйста, введите логин и пароль", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            val dialogWidth = (screenWidth * 0.95).toInt() // 95% ширины экрана

            val layoutParams = window.attributes

            layoutParams.width = dialogWidth
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

            window.setGravity(Gravity.CENTER)
            window.attributes = layoutParams
        }
    }
}