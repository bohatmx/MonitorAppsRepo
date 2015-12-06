//
//  MonitorDetailViewController.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/14.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import UIKit
import MobileCoreServices

class MonitorDetailViewController: UIViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate {

    // MARK: Properties
    var monitor:MonitorDTO?
    var response:ResponseDTO?
    
    @IBOutlet weak var mImageView: UIImageView!
    @IBOutlet weak var mMonitorName: UILabel!
    override func viewDidLoad() {
        super.viewDidLoad()

//        mImageView.layer.cornerRadius = mImageView.frame.size.width / 3;
//        mImageView.clipsToBounds = true;
//        mImageView.layer.borderWidth = 2.0
//        mImageView.layer.borderColor = UIColor.lightGrayColor().CGColor

        
        let name = monitor?.firstName
        let lname = monitor?.lastName
        mMonitorName.text = name! + " " + lname!
        if (monitor?.photoUploadList.count > 0) {
            downloadImage((monitor?.photoUploadList[0].uri)!)
        } else {
            self.mImageView.alpha = 0.1
        }
        let tapGestureRecognizer = UITapGestureRecognizer(target:self, action:Selector("useCameraRoll:"))
        mImageView.userInteractionEnabled = true
        mImageView.addGestureRecognizer(tapGestureRecognizer)
        
        getMonitorSummary()
    
    }
    
    func imageTapped(sender:AnyObject) {
        
    }
    func useCameraRoll(sender: AnyObject) {
        print("image clicked - camera request starting")
        if UIImagePickerController.isSourceTypeAvailable(
            UIImagePickerControllerSourceType.SavedPhotosAlbum) {
                let imagePicker = UIImagePickerController()
                
                imagePicker.delegate = self
                imagePicker.sourceType =
                    UIImagePickerControllerSourceType.PhotoLibrary
                imagePicker.mediaTypes = [kUTTypeImage as String]
                imagePicker.allowsEditing = false
                self.presentViewController(imagePicker, animated: true,
                    completion: nil)
        }
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    var session: NSURLSession {
        return NSURLSession.sharedSession()
    }
    
    func downloadImage(urlString:String) {
        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
        session.downloadTaskWithURL(NSURL(string: urlString)!) {url, response, error in
            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
            if let url = url {
                let downloadedPhoto = UIImage(data: NSData(contentsOfURL: url)!)
                dispatch_async(dispatch_get_main_queue()) {
                    self.mImageView.image = downloadedPhoto
                    self.mImageView.alpha = 1.0
                    self.completion(downloadedPhoto!)
                }
            }
            else {
                dispatch_async(dispatch_get_main_queue()) {
                    self.completion(nil)
                }
            }
            }.resume()
        
    }
    func completion(image:AnyObject!) {
        //print("image download completion .....")
        
    }
    
    // MARK: Delegate
    func imagePickerController(picker: UIImagePickerController!, didFinishPickingImage image: UIImage!, editingInfo: NSDictionary!){
        print("didFinishPickingImage: i've got an image");
        mImageView.image = image!
    }
    func capture(sender : AnyObject?) {
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera){
            print("Button capture")
            
            let imag = UIImagePickerController()
            imag.delegate = self
            imag.sourceType = UIImagePickerControllerSourceType.Camera;
            imag.mediaTypes = [kUTTypeImage as String]
            imag.allowsEditing = false
            
            self.presentViewController(imag, animated: true, completion: nil)
        }
    }

    
    func image(image: UIImage, didFinishSavingWithError error: NSErrorPointer, contextInfo:UnsafePointer<Void>) {
        
        if error != nil {
            let alert = UIAlertController(title: "Save Failed",
                message: "Failed to save image",
                preferredStyle: UIAlertControllerStyle.Alert)
            
            let cancelAction = UIAlertAction(title: "OK",
                style: .Cancel, handler: nil)
            
            alert.addAction(cancelAction)
            self.presentViewController(alert, animated: true,
                completion: nil)
        }
    }
    func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }

    func getMonitorSummary() {
        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
        let req = RequestDTO()
        req.cacheResponse = false
        req.zipResponse = false
        req.requestType = RequestDTO.GET_MONITOR_SUMMARY
        req.monitorID = (monitor?.monitorID)!
        let coms = Comms.sharedInstance
        coms.sendRequest(req)
            { (response) -> Void in
                UIApplication.sharedApplication().networkActivityIndicatorVisible = false
                logger.info("Yeeeeeebo! Status Code: \(response.statusCode)")
                self.response = response
                self.monitor = response.monitor
                
        }
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
