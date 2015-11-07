//
//  ViewController.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/07.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import UIKit

class SignInController: UIViewController {
    
    let req = RequestDTO()
    let indicator = UIActivityIndicatorView(activityIndicatorStyle: .Gray)
    var response:ResponseDTO = ResponseDTO()
    override func viewDidLoad() {
        super.viewDidLoad()
        print("viewDidLoad")

        let req = RequestDTO();
        req.requestType = RequestDTO.GET_STAFF_DATA
        req.staffID = 12
        
        Comms().sendRequest(req) { (response) -> Void in
            self.response = response
            print("Projects found:  \(response.projectList.count) ")
        }
    }
    func showBusy() {
        indicator.center = view.center
        view.addSubview(indicator)
        indicator.startAnimating()
    }
    func showBusyOff() {
        indicator.stopAnimating()
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


}

