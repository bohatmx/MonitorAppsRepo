//
//  StaffTableViewCell.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/15.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import UIKit

class StaffTableViewCell: UITableViewCell {

    @IBOutlet weak var mStaffName: UILabel!
    @IBOutlet weak var mImageView: UIImageView!
    @IBOutlet weak var mSubTitle: UILabel!
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
