//
//  MonitorTableViewCell.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/14.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import UIKit

class MonitorTableViewCell: UITableViewCell {

    @IBOutlet weak var mImageView: UIImageView!
    @IBOutlet weak var mMonitorName: UILabel!
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
