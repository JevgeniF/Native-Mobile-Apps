package com.fenko.connect4v2

class Game {

    var board = Array(6) { Array(7) { 0 } }
    var endGame = false
    var movesCounter = 0
    var pOneWins = 0
    var pTwoWins = 0

    fun getInt(row: Int, col: Int): Int {
        return board[row][col]
    }

    fun move(row: Int, col: Int, settings: Settings) {
        if (!endGame) {
            for (i in 5 downTo row) {
                if (getInt(i, col) == 0) {
                    if (settings.isPlayerOneMove) {
                        board[i][col] = 1
                        settings.isPlayerOneMove = false
                        movesCounter += 1
                        break
                    } else {
                        board[i][col] = 2
                        settings.isPlayerOneMove = true
                        movesCounter += 1
                        break
                    }
                }
            }
            winCheck(settings)
        }
    }

    private fun winCheck(settings: Settings) {
        for (i in 0..5) {
            for (j in 0..6) {
                if (board[i][j] != 0) {
                    if ((j <= 3)
                        && (board[i][j] == board[i][j + 1])
                        && (board[i][j] == board[i][j + 2])
                        && (board[i][j] == board[i][j + 3])
                    ) {
                        endGame = true
                        winCount(settings)
                    }
                    if ((i <= 2)
                        && (board[i][j] == board[i + 1][j])
                        && (board[i][j] == board[i + 2][j])
                        && (board[i][j] == board[i + 3][j])
                    ) {
                        endGame = true
                        winCount(settings)
                    }
                    if ((i <= 2) && (j <= 3)) {
                        if ((board[i][j] == board[i + 1][j + 1])
                            && (board[i][j] == board[i + 2][j + 2])
                            && (board[i][j] == board[i + 3][j + 3])
                        ) {
                            endGame = true
                            winCount(settings)
                        }
                    }
                    if ((i <= 2) && (j >= 3)) {
                        if ((board[i][j] == board[i + 1][j - 1])
                            && (board[i][j] == board[i + 2][j - 2])
                            && (board[i][j] == board[i + 3][j - 3])
                        ) {
                            endGame = true
                            winCount(settings)
                        }
                    }
                    if (movesCounter == 42 && !endGame) {
                        endGame = true
                    }
                }
            }
        }
    }

    private fun winCount(settings: Settings) {
        if (settings.isPlayerOneMove) {
            pTwoWins++
        } else {
            pOneWins++
        }
    }

    fun gameReset() {
        board = Array(6) { Array(7) { 0 } }
        movesCounter = 0
        endGame = false
    }
}