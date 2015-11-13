//
//  ProjTableViewCell.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/07.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import UIKit

class ProjTableViewCell: UITableViewCell {

    // MARK: Properties
    @IBOutlet weak var mProjectName: UILabel!
    @IBOutlet weak var mPhotoCount: UILabel!
    @IBOutlet weak var mStatusCount: UILabel!
    @IBOutlet weak var mImage: UIImageView!
    
    // MARK: Functions
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    // MARK: Actions

}
