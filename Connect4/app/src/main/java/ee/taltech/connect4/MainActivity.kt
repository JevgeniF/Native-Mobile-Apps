package ee.taltech.connect4

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    var nextMoveByPOne = true
    var endGame = false
    var board = arrayOf(
        arrayOf(
            2131165301,
            2131165302,
            2131165303,
            2131165304,
            2131165305,
            2131165306,
            2131165307
        ),
        arrayOf(
            2131165294,
            2131165295,
            2131165296,
            2131165297,
            2131165298,
            2131165299,
            2131165300
        ),
        arrayOf(
            2131165287,
            2131165288,
            2131165289,
            2131165290,
            2131165291,
            2131165292,
            2131165293
        ),
        arrayOf(
            2131165280,
            2131165281,
            2131165282,
            2131165283,
            2131165284,
            2131165285,
            2131165286
        ),
        arrayOf(
            2131165273,
            2131165274,
            2131165275,
            2131165276,
            2131165277,
            2131165278,
            2131165279
        ),
        arrayOf(
            2131165266,
            2131165267,
            2131165268,
            2131165269,
            2131165270,
            2131165271,
            2131165272
        )
    )
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
                for (i in 35 downTo 0 step 7) {
                    if (buttonId <= (buttonId + i) && (buttonId + i) <= 2131165307 && findViewById<Button>(
                            buttonId + i
                        ).text == ""
                    ) {
                        if (nextMoveByPOne) {
                            findViewById<Button>(buttonId + i).text = "1"
                            findViewById<Button>(buttonId + i).setBackgroundResource(R.drawable.roundedbuttonp1pressed)
                            findViewById<Button>(2131165391).setBackgroundResource(R.drawable.indicator)
                            findViewById<Button>(2131165394).setBackgroundResource(R.drawable.indicatornexttwo)
                            nextMoveByPOne = !nextMoveByPOne
                            break
                        } else {
                            findViewById<Button>(buttonId + i).text = "2"
                            findViewById<Button>(buttonId + i).setBackgroundResource(R.drawable.roundedbuttonp2pressed)
                            findViewById<Button>(2131165391).setBackgroundResource(R.drawable.indicatornextone)
                            findViewById<Button>(2131165394).setBackgroundResource(R.drawable.indicator)
                            nextMoveByPOne = !nextMoveByPOne
                            break
                        }
                    }
                }
            }
            winCheck()
        }
        if (endGame) {
            if (nextMoveByPOne) {
                println("p2wins")
            } else {
                println("p1wins")
            }
        } else if (counter == 42 && !endGame) {
            endGame = true
            println("draw")
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
