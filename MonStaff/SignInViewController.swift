//
//  SignInViewController.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/11.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import UIKit

class SignInViewController: UIViewController {

    // MARK: Properties
    @IBOutlet weak var email: UITextField!
    @IBOutlet weak var password: UITextField!
    @IBOutlet weak var btnSignIn: UIButton!
    var response: ResponseDTO?
    let defaults = NSUserDefaults.standardUserDefaults()
    override func viewDidLoad() {
        super.viewDidLoad()

        self.navigationController?.setNavigationBarHidden(true, animated: true)
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func signIn(sender: UIButton) {
        
        if email.text == "" {
            let alertView = UIAlertController(title: "Polite Reminder", message: "Please enter email address", preferredStyle: .Alert)
            alertView.addAction(UIAlertAction(title: "Ok", style: .Default, handler: nil))
            presentViewController(alertView, animated: true, completion: nil)
            return
        }
        let req = RequestDTO()
        req.zipResponse = true
        req.email = email.text!
        req.pin = password.text!
        req.requestType = RequestDTO.LOGIN_STAFF
        
        let coms = Comms()
        coms.sendRequest(req) { (response) -> Void in
            print("\nSignInViewController: Yeeeeeebo! status code: \(response.statusCode)")
            self.defaults.setValue(response.staff.staffID, forKey: "staffID")
            self.response = response
            self.navigationController?.popViewControllerAnimated(true)
        }

    }

}
