//
//  PicturesViewController.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/08.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import UIKit

class PicturesViewController: UIViewController, UICollectionViewDelegateFlowLayout, UICollectionViewDataSource {
    
    @IBOutlet weak var collectionView: UICollectionView!
    var project:ProjectDTO = ProjectDTO()
    var photoList:Array<PhotoUploadDTO> = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = project.projectName
        photoList = project.photoUploadList
        //        let layout: UICollectionViewFlowLayout = UICollectionViewFlowLayout()
        //        layout.sectionInset = UIEdgeInsets(top: 0, left: 0, bottom: 1, right: 0)
        //        layout.itemSize = CGSize(width: 200, height: 200)
        //collectionView = UICollectionView(frame: self.view.frame, collectionViewLayout: layout)
        collectionView!.dataSource = self
        collectionView!.delegate = self
        //collectionView!.registerClass(CollectionViewCell.self, forCellWithReuseIdentifier: "CollectionViewCell")
        collectionView!.backgroundColor = UIColor.whiteColor()
        //self.view.addSubview(collectionView!)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return 1
    }
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        print("numberOfItemsInSection \(photoList.count)")
        return photoList.count
    }
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier("CollectionViewCell", forIndexPath: indexPath) as! CollectionViewCell
        let photo = photoList[indexPath.row]
        //download image
        //load_image(photo.uri, cell: cell)
        downloadImage(photo.uri, cell: cell)
        cell.backgroundColor = UIColor.blackColor()
        if let theDate = NSDate(jsonDate: "/Date(\(photo.dateTaken))/")
        {
            print(theDate)
            cell.mCaption.text = "\(theDate)"
        }
        else
        {
            print("wrong format")
            cell.mCaption.text = "Date Not Computed"
        }
        
        return cell
    }
    func load_image(urlString:String, cell:CollectionViewCell)
    {
        
        let imgURL: NSURL = NSURL(string: urlString)!
        let request: NSURLRequest = NSURLRequest(URL: imgURL)
        NSURLConnection.sendAsynchronousRequest(
            request, queue: NSOperationQueue.mainQueue(),
            completionHandler: {(response: NSURLResponse?,data: NSData?,error: NSError?) -> Void in
                if error == nil {
                    cell.mImage.image = UIImage(data: data!)
                }
        })
        
    }
    var photoCache = [String:UIImage]()
    var placesTask: NSURLSessionDataTask?
    var session: NSURLSession {
        return NSURLSession.sharedSession()
    }
    
    func downloadImage(urlString:String, cell:CollectionViewCell) {
        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
        session.downloadTaskWithURL(NSURL(string: urlString)!) {url, response, error in
            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
            if let url = url {
                let downloadedPhoto = UIImage(data: NSData(contentsOfURL: url)!)
                //self.photoCache[reference] = downloadedPhoto
                dispatch_async(dispatch_get_main_queue()) {
                    cell.mImage.image = downloadedPhoto
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
    
    
    
    /*
    // MARK: - Navigation
    
    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
    // Get the new view controller using segue.destinationViewController.
    // Pass the selected object to the new view controller.
    }
    */
    
}
extension NSDate {
    convenience init?(jsonDate: String) {
        let prefix = "/Date("
        let suffix = ")/"
        let scanner = NSScanner(string: jsonDate)
        
        // Check prefix:
        if scanner.scanString(prefix, intoString: nil) {
            
            // Read milliseconds part:
            var milliseconds : Int64 = 0
            if scanner.scanLongLong(&milliseconds) {
                // Milliseconds to seconds:
                var timeStamp = NSTimeInterval(milliseconds)/1000.0
                
                // Read optional timezone part:
                var timeZoneOffset : Int = 0
                if scanner.scanInteger(&timeZoneOffset) {
                    let hours = timeZoneOffset / 100
                    let minutes = timeZoneOffset % 100
                    // Adjust timestamp according to timezone:
                    timeStamp += NSTimeInterval(3600 * hours + 60 * minutes)
                }
                
                // Check suffix:
                if scanner.scanString(suffix, intoString: nil) {
                    // Success! Create NSDate and return.
                    self.init(timeIntervalSince1970: timeStamp)
                    return
                }
            }
        }
        
        // Wrong format, return nil. (The compiler requires us to
        // do an initialization first.)
        self.init(timeIntervalSince1970: 0)
        return nil
    }
}
