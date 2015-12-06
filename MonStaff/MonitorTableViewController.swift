//
//  MonitorTableViewController.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/07.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import UIKit
import SwiftyJSON

class MonitorTableViewController: UITableViewController {
    
    // MARK: Properties
    var project:ProjectDTO?
    var monitorList: Array<MonitorDTO>?
    var photoCache = [String:UIImage]()
    var response:ResponseDTO = ResponseDTO()
    let req: RequestDTO = RequestDTO()
    var placesTask: NSURLSessionDataTask?
    let fileManager = NSFileManager.defaultManager()
    let paths = NSSearchPathForDirectoriesInDomains(.DocumentDirectory, .UserDomainMask, true)[0] as String
    
    @IBOutlet weak var mTableView: UITableView!
    var session: NSURLSession {
        return NSURLSession.sharedSession()
    }
    // MARK: Fucntions
    override func viewDidLoad() {
        super.viewDidLoad()
        
    }
    override func viewWillAppear(animated: Bool) {
        logger.debug("about to appear")
        super.viewWillAppear(animated)
        Util.setTabIndex(1)
        getCachedData()
        
    }
    func getCachedData() {
        
        response = Util.getCachedData()
        if (response.statusCode == 0) {
            monitorList = response.monitorList
            mTableView.reloadData()
            let rowToSelect:NSIndexPath = NSIndexPath(forRow: Util.getMonitorIndex(), inSection: 0);
            mTableView.scrollToRowAtIndexPath(rowToSelect, atScrollPosition: .Top, animated: true)
            self.title = "\(monitorList!.count) Monitors"
        } else {
            logger.error("Unable to get cached data")
            getRemoteData()
        }
        
    }
    func getRemoteData() {
        req.zipResponse = true
        let coms = Comms.sharedInstance
        coms.sendRequest(req) { (response) -> Void in
            logger.info("Yeeeeeebo! status code: \(response.statusCode)")
            self.response = response
            self.monitorList = response.monitorList
            self.mTableView.reloadData()
            self.cacheData()
            self.title = "\(self.monitorList!.count) Monitors"
        }
        
    }
    func cacheData() {
        
        let filePathToWrite = "\(paths)/data.json"
        let data = response.json.dataUsingEncoding(NSUTF8StringEncoding)
        fileManager.createFileAtPath(filePathToWrite, contents: data, attributes: nil)
        logger.debug("cache data saved")
    }
    
    func downloadImage(urlString:String, cell:MonitorTableViewCell) {
        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
        session.downloadTaskWithURL(NSURL(string: urlString)!) {url, response, error in
            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
            if let url = url {
                let downloadedPhoto = UIImage(data: NSData(contentsOfURL: url)!)
                //self.photoCache[reference] = downloadedPhoto
                dispatch_async(dispatch_get_main_queue()) {
                    cell.mImageView.image = downloadedPhoto
                    cell.mImageView.alpha = 1.0
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
        
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: - Table view data source
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return (monitorList?.count)!
    }
    
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("MonitorTableViewCell", forIndexPath: indexPath) as! MonitorTableViewCell
        
        let monitor = monitorList![indexPath.row]
        cell.mMonitorName.text = "\(monitor.firstName) \(monitor.lastName)"
        if monitor.photoUploadList.count > 0 {
            let url = monitor.photoUploadList[0].uri
            downloadImage(url, cell: cell)
        } else {
            cell.mImageView.alpha = 0.1
        }
        
        cell.mImageView.layer.cornerRadius = cell.mImageView.frame.size.width / 2;
        cell.mImageView.clipsToBounds = true;
        
        return cell
    }
    // MARK: - Navigation
    var selectedMonitor:MonitorDTO?
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        Util.setMonitorIndex(indexPath.row)
        selectedMonitor = monitorList![indexPath.row]
        
        let mapController = self.storyboard?.instantiateViewControllerWithIdentifier("MonitorDetailViewController") as! MonitorDetailViewController
        mapController.monitor = selectedMonitor!
        
        self.navigationController?.pushViewController(mapController, animated: true)
    }
    
    
    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }    
    
}
