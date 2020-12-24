package com.fenko.connect4v2

import android.app.Dialog
import android.content.Context
import android.widget.*

class Settings {

    var playerOneName: String? = null
    var playerTwoName: String? = null

    var isSettingsWereOpened = false

    var isPlayerOneMove = true

    var colorTheme = 0

    var pOneBackground = R.drawable.default_p_one_stat
    var pTwoBackground = R.drawable.default_p_two_stat
    var boardBackground = R.drawable.default_board

    fun askSettings(context: Context, mainActivity: MainActivity) {

        val dialog = Dialog(context)

        dialog.setContentView(R.layout.settings_pop_up)
        dialog.show()
        controlThemeButtons(dialog)

        dialog.findViewById<EditText>(R.id.editTextPoneName).setText(playerOneName)
        dialog.findViewById<EditText>(R.id.editTextPtwoName).setText(playerTwoName)

        val switcherFirstMove = dialog.findViewById(R.id.switchFirstMove) as CompoundButton
        switcherFirstMove.setOnCheckedChangeListener { _, isChecked ->
            isPlayerOneMove = !isChecked
        }

        val checkBoxDefaultTheme = dialog.findViewById(R.id.checkBoxDefaultTheme) as CheckBox
        val checkBoxZephyrTheme = dialog.findViewById(R.id.checkBoxZephyrTheme) as CheckBox
        val checkBoxAkaverTheme = dialog.findViewById(R.id.checkBoxAkaverTheme) as CheckBox
        checkBoxDefaultTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                colorTheme = 0
                controlThemeButtons(dialog)
            }
        }
        checkBoxZephyrTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                colorTheme = 1
                controlThemeButtons(dialog)
            }
        }
        checkBoxAkaverTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                colorTheme = 2
                controlThemeButtons(dialog)
            }
        }

        val buttonClose = dialog.findViewById(R.id.buttonOK) as Button

        buttonClose.setOnClickListener {
            val tPlayerOneName = dialog.findViewById(R.id.editTextPoneName) as EditText
            playerOneName = tPlayerOneName.text.toString()

            val tPlayerTwoName = dialog.findViewById(R.id.editTextPtwoName) as EditText
            playerTwoName = tPlayerTwoName.text.toString()

            dialog.dismiss()
            Toast.makeText(context,
                context.resources.getString(R.string.toastMessage), Toast.LENGTH_SHORT).show()

            setTheme()
            mainActivity.updateUI()
        }
    }

    private fun controlThemeButtons(dialog: Dialog) {
        val checkBoxDefaultTheme = dialog.findViewById(R.id.checkBoxDefaultTheme) as CheckBox
        val checkBoxZephyrTheme = dialog.findViewById(R.id.checkBoxZephyrTheme) as CheckBox
        val checkBoxAkaverTheme = dialog.findViewById(R.id.checkBoxAkaverTheme) as CheckBox

        when (colorTheme) {
            0 -> {
                checkBoxDefaultTheme.isChecked = true
                checkBoxZephyrTheme.isChecked = false
                checkBoxAkaverTheme.isChecked = false
            }
            1 -> {
                checkBoxDefaultTheme.isChecked = false
                checkBoxZephyrTheme.isChecked = true
                checkBoxAkaverTheme.isChecked = false
            }
            2 -> {
                checkBoxDefaultTheme.isChecked = false
                checkBoxZephyrTheme.isChecked = false
                checkBoxAkaverTheme.isChecked = true
            }
        }

        if (!checkBoxDefaultTheme.isChecked && !checkBoxZephyrTheme.isChecked
            && !checkBoxAkaverTheme.isChecked
        ) {
            colorTheme = 0
        }
    }

    private fun setTheme() {

        when (colorTheme) {
            0 -> {
                boardBackground = R.drawable.default_board
                pOneBackground = R.drawable.default_p_one_stat
                pTwoBackground = R.drawable.default_p_two_stat

            }
            1 -> {
                boardBackground = R.drawable.zephyr_board
                pOneBackground = R.drawable.zephyr_p_one_stat
                pTwoBackground = R.drawable.zephyr_p_two_stat
            }
            2 -> {
                boardBackground = R.drawable.akaver_board
                pOneBackground = R.drawable.akaver_p_one_stat
                pTwoBackground = R.drawable.akaver_p_two_stat
            }
        }
    }
}