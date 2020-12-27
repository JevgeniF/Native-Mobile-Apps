//
//  ConnectFour.swift
//  Connect4_iOS
//
//  Created by Jevgeni Fenko on 27.11.2020.
//

import Foundation

class ConnectFour {
    
    var gameBoard: [[Chip?]] = Array(repeating: Array(repeating: nil, count: 7), count: 6)
    
    var nextMoveByPOne = true
    var endGame = false
    var counter = 0
    var pOneCounter = 0
    var pTwoCounter = 0
    
    
    func getChip(rowNo row:Int, colNo col:Int) -> Chip? {
        return gameBoard[row][col]
    }
    
    func move(rowNo row: Int, colNo col: Int) {
        if !endGame {
            for i in (row...5).reversed() {
                if getChip(rowNo: i, colNo: col) == nil {
                    gameBoard[i][col] = Chip(isPOne: nextMoveByPOne)
                    nextMoveByPOne = !nextMoveByPOne
                    counter += 1
                    break
                }
            }
            winCheck()
        }
    }
    
    func winCheck() {
        for i in (0...5) {
            for j in (0...6) {
                if gameBoard[i][j] != nil {
                    //3 to right
                    if (j <= 3)
                        && (gameBoard[i][j] == gameBoard[i][j + 1])
                        && (gameBoard[i][j] == gameBoard[i][j + 2])
                        && (gameBoard[i][j] == gameBoard[i][j + 3]) {
                            endGame = true
                            winCount()
                    }
                    //3 to top
                    if (i <= 2)
                        && (gameBoard[i][j] == gameBoard[i + 1][j])
                        && (gameBoard[i][j] == gameBoard[i + 2][j])
                        && (gameBoard[i][j] == gameBoard[i + 3][j]) {
                            endGame = true
                            winCount()
                    }
                    //3 to top right
                    if (i <= 2) && (j <= 3) {
                        if (gameBoard[i][j] == gameBoard[i + 1][j + 1])
                            && (gameBoard[i][j] == gameBoard[i + 2][j + 2])
                            && (gameBoard[i][j] == gameBoard[i + 3][j + 3]) {
                                endGame = true
                                winCount()
                        }
                    }
                    //3 to top left
                    if (i <= 2) && (j >= 3) {
                        if (gameBoard[i][j] == gameBoard[i + 1][j - 1])
                            && (gameBoard[i][j] == gameBoard[i + 2][j - 2])
                            && (gameBoard[i][j] == gameBoard[i + 3][j - 3]) {
                                endGame = true
                                winCount()
                        }
                    }
                    //draw!
                    if (counter == 42) && (!endGame) {
                        endGame = true
                    }
                }
            }
        }
    }
    
    func winCount() {
        if nextMoveByPOne {
            pTwoCounter += 1
        } else {
            pOneCounter += 1
        }
    }
    
    func reset(){
        for i in (0...5) {
            for j in (0...6) {
                gameBoard[i][j] = nil
                counter = 0
                endGame = false
            }
        }
    }
}
