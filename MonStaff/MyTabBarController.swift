//
//  MyTabBarController.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/14.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import UIKit

class MyTabBarController: UITabBarController {

    override func viewDidLoad() {
        super.viewDidLoad()
        logger.debug("######## MonStaff App Started ###############")
        self.selectedIndex = Util.getTabIndex()

    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
}
