package com.river.walklog.feature.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.river.walklog.feature.settings.databinding.FragmentNicknameEditBinding
import com.river.walklog.core.designsystem.R as DesignR

class NicknameEditBottomSheet : AppCompatDialogFragment() {

    private var _binding: FragmentNicknameEditBinding? = null
    private val binding get() = _binding!!

    var onSave: ((String) -> Unit)? = null

    private val currentNickname get() = arguments?.getString(ARG_NICKNAME).orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNicknameEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyNavigationBarInsets()
        setupTextField()
        setupSaveButton()
        openKeyboard()
    }

    @Suppress("DEPRECATION")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also { dialog ->
            dialog.window?.apply {
                setGravity(Gravity.BOTTOM)
                setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE,
                )
                setBackgroundDrawableResource(android.R.color.transparent)
                setWindowAnimations(R.style.NicknameSheetAnimation)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun applyNavigationBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.navBarSpacer) { view, insets ->
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.updatePadding(bottom = navBarHeight)
            insets
        }
    }

    private fun setupTextField() = with(binding) {
        etNickname.apply {
            setText(currentNickname)
            setSelection(currentNickname.length)
            filters = arrayOf(
                InputFilter { source, _, _, _, _, _ ->
                    if (source.all { it.isKorean() || it in 'a'..'z' || it in 'A'..'Z' }) source else ""
                },
                InputFilter.LengthFilter(MAX_NICKNAME_LENGTH),
            )
            addTextChangedListener(NicknameWatcher())
            setOnFocusChangeListener { _, hasFocus ->
                flNicknameContainer.setBackgroundResource(
                    if (hasFocus) {
                        R.drawable.bg_nickname_field_focused
                    } else {
                        R.drawable.bg_nickname_field
                    },
                )
            }
        }
        updateCounter(currentNickname.length)
        btnSave.isEnabled = false
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val newNickname = binding.etNickname.text.toString().trim()
            onSave?.invoke(newNickname)
            dismiss()
        }
    }

    private fun openKeyboard() {
        binding.etNickname.post {
            binding.etNickname.requestFocus()
            val imm = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etNickname, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCounter(count: Int) {
        binding.tvCounter.text = "$count/$MAX_NICKNAME_LENGTH"
        val counterColor = if (count >= MAX_NICKNAME_LENGTH) {
            ContextCompat.getColor(requireContext(), DesignR.color.walklog_primary_dark)
        } else {
            ContextCompat.getColor(requireContext(), DesignR.color.walklog_gray_300)
        }
        binding.tvCounter.setTextColor(counterColor)
    }

    private inner class NicknameWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable?) {
            val trimmed = s.toString().trim()
            updateCounter(s?.length ?: 0)
            binding.btnSave.isEnabled = trimmed.isNotEmpty() && trimmed != currentNickname
        }
    }

    companion object {
        const val TAG = "NicknameEditBottomSheet"
        private const val ARG_NICKNAME = "arg_nickname"
        private const val MAX_NICKNAME_LENGTH = 10

        fun newInstance(currentNickname: String) = NicknameEditBottomSheet().apply {
            arguments = Bundle().apply { putString(ARG_NICKNAME, currentNickname) }
        }
    }
}

private fun Char.isKorean(): Boolean = this in '가'..'힣' || this in 'ㄱ'..'ㅣ'
