package ee.taltech.connect4

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {
    var nextMoveByPOne = true
    var endGame = false
    var board = Board().board
    var counter = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    @SuppressLint("ResourceType")
    fun gameButtonOnClick(view: View) {
        if (!endGame) {
            counter++
            if ((view as Button).text == "") {
                val buttonId = view.id
                println(buttonId)
                for (i in 35 downTo 0 step 7) {
                    if (buttonId <= (buttonId + i) && (buttonId + i) <= 2131230850 && findViewById<Button>(
                            buttonId + i
                        ).text == ""
                    ) {
                        if (nextMoveByPOne) {
                            findViewById<Button>(buttonId + i).text = "1"
                            findViewById<Button>(buttonId + i).setBackgroundResource(R.drawable.roundedbuttonp1pressed)
                            findViewById<Button>(2131231009).setBackgroundResource(R.drawable.indicator)
                            findViewById<Button>(2131231010).setBackgroundResource(R.drawable.indicatornexttwo)
                            nextMoveByPOne = !nextMoveByPOne
                            break
                        } else {
                            findViewById<Button>(buttonId + i).text = "2"
                            findViewById<Button>(buttonId + i).setBackgroundResource(R.drawable.roundedbuttonp2pressed)
                            findViewById<Button>(2131231009).setBackgroundResource(R.drawable.indicatornextone)
                            findViewById<Button>(2131231010).setBackgroundResource(R.drawable.indicator)
                            nextMoveByPOne = !nextMoveByPOne
                            break
                        }
                    }
                }
            }
        }
        winCheck()
        if (endGame) {
            if (nextMoveByPOne) {
                Snackbar.make(findViewById(R.id.main), "Player Two Wins. Player One Sucks", Snackbar.LENGTH_LONG).show()
            } else {
                println("p1wins")
                Snackbar.make(findViewById(R.id.main), "Player One Wins. Nah, You're so clever as you think, Player Two", Snackbar.LENGTH_LONG).show()
            }
        } else if (counter == 42 && !endGame) {
            endGame = true
            Snackbar.make(findViewById(R.id.board), "It's a Draw. Can you imagine more stupid situation? What a waste of time", Snackbar.LENGTH_LONG).show()
        }
    }

    fun winCheck() {
        for (row in board.indices step 1) {
            for (col in board[row].indices step 1) {
                val currentId = board[row][col]
                if (findViewById<Button>(currentId).text != "") {
                    //3 to right
                    if (col <= board[row].size - 4 && findViewById<Button>(currentId).text == findViewById<Button>(
                            board[row][col + 1]
                        ).text && findViewById<Button>(currentId).text == findViewById<Button>(
                            board[row][col + 2]
                        ).text && findViewById<Button>(
                            currentId
                        ).text == findViewById<Button>(board[row][col + 3]).text
                    ) {
                        endGame = true
                    }
                    //3 to top
                    if (row <= board.size - 4 && findViewById<Button>(currentId).text == findViewById<Button>(
                            board[row + 1][col]
                        ).text && findViewById<Button>(currentId).text == findViewById<Button>(
                            board[row + 2][col]
                        ).text && findViewById<Button>(
                            currentId
                        ).text == findViewById<Button>(board[row + 3][col]).text
                    ) {
                        endGame = true
                    }
                    //3 to top right
                    if (row <= board.size - 4 && col <= board[row].size - 4) {
                        if (findViewById<Button>(currentId).text == findViewById<Button>(
                                board[row + 1][col + 1]
                            ).text && findViewById<Button>(currentId).text == findViewById<Button>(
                                board[row + 2][col + 2]
                            ).text && findViewById<Button>(
                                currentId
                            ).text == findViewById<Button>(board[row + 3][col + 3]).text
                        ) {
                            endGame = true
                        }
                    }
                    //3 to top on left
                    if (row <= board.size - 4 && col >= 3) {
                        if (findViewById<Button>(currentId).text == findViewById<Button>(
                                board[row + 1][col - 1]
                            ).text && findViewById<Button>(currentId).text == findViewById<Button>(
                                board[row + 2][col - 2]
                            ).text && findViewById<Button>(
                                currentId
                            ).text == findViewById<Button>(board[row + 3][col - 3]).text
                        ) {
                            endGame = true
                        }
                    }
                }
            }
        }
    }
}
