package com.fenko.connect4v2

class Game {
    /*
    Game Logic. Responds for moves, win state check, counters, game reset
     */

    var board = Array(6) { Array(7) { 0 } }  //game board
    var endGame = false                                 //status of game(if win or draw, game stopped)
    var movesCounter = 0                                //counter of moves per game
    var pOneWins = 0                                    //counter of wins for Player one
    var pTwoWins = 0                                    //counter of wins for Player two

    fun getInt(row: Int, col: Int): Int {
        //function returns integer-marker from board square(chip)
        return board[row][col]
    }

    fun move(row: Int, col: Int, settings: Settings) {
        /*
        function switches moves and marks board by squares(chips) by proper integer when game not stopped.
        as, by rules of gravitation, first must be filled square at bottom of field, user may press on
        every square and function will check, if the squares below are already filled.
        Example:
        pressed square at top row (5), if square at row 0 is empty, square at row 0 marked by
        player's marker-integer (1 or 2)... if square at row 0 is marked already, square at row 1 will
        be marked by player's marker-integer, and so on...
         */
        if (!endGame) {
            for (i in 5 downTo row) {
                if (getInt(i, col) == 0) {
                    // if player 1 move
                    if (settings.isPlayerOneMove) {
                        //marks as square by 1
                        board[i][col] = 1
                        //changes move indicator
                        settings.isPlayerOneMove = false
                        //counts move
                        movesCounter += 1
                        break
                    } else {
                        //same logic fo player 2
                        board[i][col] = 2
                        settings.isPlayerOneMove = true
                        movesCounter += 1
                        break
                    }
                }
            }
            //after each move game state checked
            winCheck(settings)
        }
    }

    private fun winCheck(settings: Settings) {
        //function checks game state. if 3 squares from iterated square are the same - win.
        //if board is filled and no moves left - draw
        for (i in 0..5) {
            for (j in 0..6) {
                //3 to right check
                if (board[i][j] != 0) {
                    if ((j <= 3)
                        && (board[i][j] == board[i][j + 1])
                        && (board[i][j] == board[i][j + 2])
                        && (board[i][j] == board[i][j + 3])
                    ) {
                        //game stop
                        endGame = true
                        //Winner's win counter +1
                        winCount(settings)
                    }
                    //3 to top
                    if ((i <= 2)
                        && (board[i][j] == board[i + 1][j])
                        && (board[i][j] == board[i + 2][j])
                        && (board[i][j] == board[i + 3][j])
                    ) {
                        endGame = true
                        winCount(settings)
                    }
                    //3 to top right
                    if ((i <= 2) && (j <= 3)) {
                        if ((board[i][j] == board[i + 1][j + 1])
                            && (board[i][j] == board[i + 2][j + 2])
                            && (board[i][j] == board[i + 3][j + 3])
                        ) {
                            endGame = true
                            winCount(settings)
                        }
                    }
                    //3 to top left
                    if ((i <= 2) && (j >= 3)) {
                        if ((board[i][j] == board[i + 1][j - 1])
                            && (board[i][j] == board[i + 2][j - 2])
                            && (board[i][j] == board[i + 3][j - 3])
                        ) {
                            endGame = true
                            winCount(settings)
                        }
                    }
                    //if moves counter = 42, board is filled, no moves left, no win state found - draw
                    if (movesCounter == 42 && !endGame) {
                        endGame = true
                    }
                }
            }
        }
    }

    private fun winCount(settings: Settings) {
        //function increments wins count for players, depends on who won current game
        if (settings.isPlayerOneMove) {
            //if game stopped and next move should be done by player 1, then player 2 won
            pTwoWins++
        } else {
            //if game stopped and next move should be done by player 2, then player 1 won
            pOneWins++
        }
    }

    fun gameReset() {
        //function resets game board, counters, and game stop boolean
        board = Array(6) { Array(7) { 0 } }
        movesCounter = 0
        endGame = false
    }
}