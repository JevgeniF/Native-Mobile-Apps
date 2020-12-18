//
//  Chip.swift
//  Connect4_iOS
//
//  Created by Jevgeni Fenko on 27.11.2020.
//

import Foundation

struct Chip: Equatable {
    var isPOne: Bool
    
    init(isPOne: Bool) {
        self.isPOne = isPOne
    }
}
