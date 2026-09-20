package com.verso.dict.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.verso.dict.R
import com.verso.dict.data.Word
import com.verso.dict.util.FontSizeManager

/**
 * Shows a single word and its definition. Font sizes follow the global font setting so it
 * stays consistent with the results list.
 */
class WordDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_WORD = "word"
        private const val ARG_DEF = "definition"

        fun newInstance(word: Word): WordDialogFragment {
            return WordDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_WORD, word.word)
                    putString(ARG_DEF, word.definition)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val word = arguments?.getString(ARG_WORD).orEmpty()
        val definition = arguments?.getString(ARG_DEF).orEmpty()

        val view = layoutInflater.inflate(R.layout.dialog_word, null)
        val tvWord = view.findViewById<android.widget.TextView>(R.id.tv_dialog_word)
        val tvDef = view.findViewById<android.widget.TextView>(R.id.tv_dialog_definition)
        tvWord.text = word
        tvDef.text = definition
        FontSizeManager.applyScaled(requireContext(), tvWord, R.dimen.word_text)
        FontSizeManager.applyScaled(requireContext(), tvDef, R.dimen.definition_text)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton(R.string.appreciate_close, null)
            .create()
        // On phone-class screens (sw >= 320dp) swap the AlertDialog window background for
        // the liquid-glass drawable (translucent fill + edge highlight + top sheen, no real-time
        // blur). On the OPPO Watch (sw160dp) the bool is false, so the standard AlertDialog
        // window background is kept for maximum readability and zero blur cost.
        if (requireContext().resources.getBoolean(R.bool.use_glass_dialog)) {
            dialog.window?.setBackgroundDrawableResource(R.drawable.bg_word_dialog)
        }
        return dialog
    }
}
