package com.fenko.connect4v2

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
  /*
  Main activity class, also responds for UI. Reflects all changes made in setting and by game logic.
   */
    private var settings = Settings()       // game settings
    private var game = Game()               // game logic

    override fun onSaveInstanceState(outState: Bundle) {
        //function saves game to keep same state after screen rotation
        super.onSaveInstanceState(outState)

        //saves Settings
        outState.putStringArrayList("Players",
            arrayListOf(settings.playerOneName, settings.playerTwoName))
        outState.putBoolean("SettingsOpened", settings.isSettingsWereOpened)
        outState.putBoolean("NextMove", settings.isPlayerOneMove)
        outState.putIntegerArrayList("ThemeSettings",
            arrayListOf(settings.colorTheme,
                settings.boardBackground,
                settings.pOneBackground,
                settings.pTwoBackground))

        //saves Game
        for (i in 0..5) {
            outState.putIntArray("$i", game.board[i].toIntArray())
        }
        outState.putIntegerArrayList("Counters",
            arrayListOf(game.movesCounter, game.pOneWins, game.pTwoWins))
        outState.putBoolean("EndGameState", game.endGame)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //function initializes/redraws ui on game start, screen rotation
        super.onCreate(savedInstanceState)

        settings.playerOneName = resources.getString(R.string.playerOneName)  //Player one name, can be changed in settings
        settings.playerTwoName = resources.getString(R.string.playerTwoName)  //Player two name, can be changed in settings

        if (savedInstanceState != null) {
            //if game saved, then restores on screen rotation.

                //restores Settings
            val playersList = savedInstanceState.getStringArrayList("Players")
            settings.playerOneName = playersList!![0]
            settings.playerTwoName = playersList[1]
            settings.isSettingsWereOpened = savedInstanceState.getBoolean("SettingsOpened")
            settings.isPlayerOneMove = savedInstanceState.getBoolean("NextMove")
            val themeSettings = savedInstanceState.getIntegerArrayList("ThemeSettings")
            settings.colorTheme = themeSettings!![0]
            settings.boardBackground = themeSettings[1]
            settings.pOneBackground = themeSettings[2]
            settings.pTwoBackground = themeSettings[3]

            //restores Game
            for (i in 0..5) {
                game.board[i] = savedInstanceState.getIntArray("$i")!!.toList().toTypedArray()
            }
            val counters = savedInstanceState.getIntegerArrayList("Counters")
            game.movesCounter = counters!![0]
            game.pOneWins = counters[1]
            game.pTwoWins = counters[2]
            game.endGame = savedInstanceState.getBoolean("EndGameState")
        }

        setContentView(R.layout.activity_main)
        //ui update on basis of game/settings state
        updateUI()

        // settings pop-up on game start, works when settings weren't opened yet
        if (!settings.isSettingsWereOpened) {
            settings.askSettings(this, this)
            settings.isSettingsWereOpened = true
        }
    }

    fun boardChipOnClick(view: View) {
        //function returns row/column for current click and starts move function from game logic.
        //updates ui after each move
        val (row, col) = getRowCol(view.tag)
        game.move(row, col, settings)
        updateUI()
    }

    fun butOneMoreGameOnClick(view: View) {
        //function starts new game, but doesn't reset wins statistics (as another round)
        if (game.endGame) {
            game.gameReset()
            for (i in 0..41) {
                val chip = findViewById<Button>(resources.getIdentifier("b$i", "id", packageName))
                chip.setBackgroundResource(R.drawable.board_chips)
            }
        }
    }

    fun butResetGameOnClick(view: View) {
        //function totally resets game, including statistics
        game.pOneWins = 0
        game.pTwoWins = 0
        settings.isPlayerOneMove = true
        settings.playerOneName = resources.getString(R.string.playerOneName)
        settings.playerTwoName = resources.getString(R.string.playerTwoName)
        game.gameReset()
        for (i in 0..41) {
            val chip = findViewById<Button>(resources.getIdentifier("b$i", "id", packageName))
            chip.setBackgroundResource(R.drawable.board_chips)
        }
        updateUI()
    }

    fun butSettingsOnClick(view: View) {
        // opens settings popup
        settings.askSettings(this, this)
    }

    fun updateUI() {
        //updates ui, stats, moves, game state and sends win/draw alert
        findViewById<TextView>(R.id.textStatsPoneName).text = settings.playerOneName
        findViewById<TextView>(R.id.textStatsPtwoName).text = settings.playerTwoName
        findViewById<View>(R.id.layoutPone).setBackgroundResource(settings.pOneBackground)
        findViewById<View>(R.id.layoutPtwo).setBackgroundResource(settings.pTwoBackground)
        findViewById<View>(R.id.layoutBoard).setBackgroundResource(settings.boardBackground)
        findViewById<TextView>(R.id.textStatsPoneWinsNo).text = game.pOneWins.toString()
        findViewById<TextView>(R.id.textStatsPtwoWinsNo).text = game.pTwoWins.toString()
        sendAlert(this)

        for (i in 0..41) {
            val (row, col) = getRowCol(i)
            val int = game.getInt(row, col)
            val chip = findViewById<Button>(resources.getIdentifier("b$i", "id", packageName))
            when (int) {
                1 -> {
                    chip.text = "1"
                }
                2 -> {
                    chip.text = "2"
                }
                else -> {
                    chip.text = "0"
                }
            }
            if (chip.text == "1") {
                chip.setBackgroundResource(settings.pOneBackground)
            } else if (chip.text == "2") {
                chip.setBackgroundResource(settings.pTwoBackground)
            }
            if (settings.isPlayerOneMove) {
                findViewById<View>(R.id.layoutPone).setBackgroundResource(settings.pOneBackground)
                findViewById<View>(R.id.layoutPtwo).setBackgroundResource(R.drawable.not_your_turn)
            } else {
                findViewById<View>(R.id.layoutPone).setBackgroundResource(R.drawable.not_your_turn)
                findViewById<View>(R.id.layoutPtwo).setBackgroundResource(settings.pTwoBackground)
            }
        }
    }

    private fun sendAlert(context: Context) {
        //function creates alert pop-un on game end
        if (game.endGame) {
            var message = ""
            val congratulation = resources.getString(R.string.congratulations)

            if (settings.isPlayerOneMove) {
                val messagePtwoWin = resources.getString(R.string.messageForPtwo)

                message =
                    congratulation + " " + settings.playerTwoName + "!\n" + messagePtwoWin
            } else if (game.endGame && !settings.isPlayerOneMove) {
                val messagePoneWin = resources.getString(R.string.messageForPone)
                message =
                    congratulation + " " + settings.playerOneName + "!\n" + messagePoneWin
            } else if (game.movesCounter == 42) {
                val messageDraw = resources.getString(R.string.messageDraw)
                message = messageDraw
            }

            val builder = AlertDialog.Builder(context)
            builder.setTitle(resources.getString(R.string.winDialogTitle)).setMessage(message)
                .setPositiveButton(R.string.winDialogButton) { _: DialogInterface, _: Int ->
                }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun getRowCol(tag: Any?): Pair<Int, Int> {
        //function to indicate row/column of square/chip
        val tagString = tag.toString()
        val tagInt = tagString.toInt()
        val row = tagInt / 7
        val col = tagInt - row * 7
        return Pair(row, col)
    }
}

