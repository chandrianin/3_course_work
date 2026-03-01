package com.example.bfuhelper.view

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.bfuhelper.R
import com.example.bfuhelper.model.sport.SportDataBase
import com.example.bfuhelper.viewModel.SportViewModel

class LoginDialogFragment : DialogFragment() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var externalLinkTextView: TextView


    // Объявляем ViewModel, чтобы получить доступ к ней
    private lateinit var sportViewModel: SportViewModel

    companion object {
        const val TAG = "LoginDialogFragment"

        fun newInstance(): LoginDialogFragment {
            return LoginDialogFragment()
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
        Log.d(TAG, "LoginDialogFragment ViewModel instance: ${sportViewModel.hashCode()}")

        // Диалог неубираем по нажатию вне или кнопкой "Назад"
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Устанавливаем макет
        return inflater.inflate(R.layout.dialog_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем внешний вид диалога (прозрачный фон, чтобы был виден rounded_dialog_background)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Инициализация элементов UI
        usernameEditText = view.findViewById(R.id.username_edit_text)
        passwordEditText = view.findViewById(R.id.password_edit_text)
        loginButton = view.findViewById(R.id.login_button)
        externalLinkTextView = view.findViewById(R.id.external_link_textview_manual)

        val externalLinkTextView = view.findViewById<TextView>(R.id.external_link_textview_manual)
        externalLinkTextView.setOnClickListener {
            val url = "https://kantiana.ru/universitys/personal-data/" // Здесь ваш целевой URL
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        // Кнопка "Войти"
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            Log.d(tag, "Нажата 'Войти': $username, $password")

            if (username.isNotEmpty() && password.isNotEmpty()) {

                sportViewModel.saveAndLogin(username, password)
                // Диалог будет автоматически закрыт, когда ViewModel обновит isLoginDataMissing на false
                // в SportFragment, который наблюдает за ним.
            } else {
                Toast.makeText(context, "Пожалуйста, введите логин и пароль", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window -> // Получаем метрики дисплея
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels // Ширина экрана в пикселях

            // Определяем желаемую ширину диалога (например, 85% от ширины экрана)
            val dialogWidth = (screenWidth * 0.85).toInt() // 85% ширины экрана

            val layoutParams = window.attributes

            // Устанавливаем вычисленную ширину для окна диалога
            layoutParams.width = dialogWidth
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT // Высота по содержимому

            // Установка гравитации окна в центр
            window.setGravity(Gravity.CENTER) // Отцентрирует диалог по горизонтали и вертикали

            window.attributes = layoutParams
        }
    }
}