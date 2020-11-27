//
//  ViewController.swift
//  Connect4_iOS
//
//  Created by Jevgeni Fenko on 26.11.2020.
//

import UIKit

class ViewController: UIViewController {
    
    var game: ConnectFour = ConnectFour()
    
    @IBOutlet var gameChips: [UIButton]!
    @IBAction func GameChipsClicked(_ sender: UIButton) {
        let (row, col) = getRowCol(chipNo: sender.tag)
        game.move(rowNo: row, colNo: col)
        updateUI()
    }
    
    @IBOutlet weak var moveByPOne: UIView!
    @IBOutlet var moveByPOneText: [UILabel]!
    
    @IBOutlet weak var moveByPTwo: UIView!
    @IBOutlet var moveByPTwoText: [UILabel]!
    
    @IBOutlet weak var pOneCounter: UILabel!
    @IBOutlet weak var pTwoCounter: UILabel!
    
    @IBAction func oneMoreGame(_ sender: UIButton) {
        oneMoreGame()
    }
    
    @IBAction func resetGame(_ sender: UIButton) {
        resetGame()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        updateUI()
        sendAlert()
        
    }
    
    override open var shouldAutorotate: Bool {
        return false
    }
    
    override open var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return.portrait
    }
    
    func updateUI(){
        pOneCounter.text = "\(game.pOneCounter)"
        pTwoCounter.text = "\(game.pTwoCounter)"
        sendAlert()
        for gameChip in gameChips {
            let(row, col) = getRowCol(chipNo: gameChip.tag)
            let chip = game.getChip(rowNo: row, colNo: col)
            if chip == nil {
                gameChip.setTitle("", for: UIControl.State.normal)
            } else {
                gameChip.setTitle(chip!.isPOne ? "1" : "2", for: UIControl.State.normal)
                
            }
            
            if gameChip.currentTitle == "1" {
                gameChip.backgroundColor = UIColor.systemPink;
            } else if gameChip.currentTitle == "2" {
                gameChip.backgroundColor = UIColor.systemOrange
            }
            
            if game.nextMoveByPOne {
                moveByPOne.backgroundColor = UIColor.systemPink;
                for moveByPOneT in moveByPOneText {
                    moveByPOneT.textColor = UIColor.white
                }
                moveByPTwo.backgroundColor = UIColor.white
                for moveByPTwoT in moveByPTwoText {
                    moveByPTwoT.textColor = UIColor.black
                }
            } else {
                moveByPOne.backgroundColor = UIColor.white;
                for moveByPOneT in moveByPOneText {
                    moveByPOneT.textColor = UIColor.black
                }
                moveByPTwo.backgroundColor = UIColor.systemOrange
                for moveByPTwoT in moveByPTwoText {
                    moveByPTwoT.textColor = UIColor.white
                }
            }
        }
    }
    
    func sendAlert() {
        var notice = ""
        if game.counter == 42 {
            notice = "What can be more boring than DRAW in Connect Four? Use your brains and avoid it in future!"
        }
        if game.counter != 42 {
            if game.nextMoveByPOne {
                notice = "Player Two is the smartest player in the World! Remember this, looser!"
            } else {
                notice = "Who is the winer? Player One is the winner! You all suck!"
            }
        }
        let alert = UIAlertController(title: "Game Over", message: notice, preferredStyle: UIAlertController.Style.alert)
        alert.addAction(UIAlertAction(title: "One More Game", style: UIAlertAction.Style.default, handler: {(action: UIAlertAction!) in self.oneMoreGame()}))
        alert.addAction(UIAlertAction(title: "Game Reset", style: UIAlertAction.Style.default, handler: {(action: UIAlertAction!) in self.resetGame()}))
        if game.endGame {
            self.present(alert, animated: true, completion: nil)
        }
    }
        
    func getRowCol(chipNo: Int) -> (row: Int, col: Int) {
        let rowNo = chipNo/7
        let colNo = chipNo - rowNo*7
        return(rowNo, colNo)
    }
    
    func oneMoreGame() {
        if game.endGame {
            game.reset()
            for gameChip in gameChips {
                gameChip.backgroundColor = UIColor.white
            }
        }
        updateUI()
    }
    
    func resetGame() {
        game.pOneCounter = 0;
        game.pTwoCounter = 0;
        game.nextMoveByPOne = true
        game.reset()
        for gameChip in gameChips {
            gameChip.backgroundColor = UIColor.white
        }
        updateUI()
    }
}

