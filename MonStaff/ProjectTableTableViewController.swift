//
//  ProjectTableTableViewController.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/07.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import UIKit
import SwiftyJSON
import XCGLogger

class ProjectTableTableViewController: UITableViewController {

    // MARK: Properties
    let req = RequestDTO()
    var projectList: Array<ProjectDTO> = []
    var selectedProject:ProjectDTO?
    let defaults = NSUserDefaults.standardUserDefaults()
    let indicator = UIActivityIndicatorView(activityIndicatorStyle: .Gray)
    var response:ResponseDTO = ResponseDTO()
    let fileManager = NSFileManager.defaultManager()
    let paths = NSSearchPathForDirectoriesInDomains(.DocumentDirectory, .UserDomainMask, true)[0] as String
    
    @IBOutlet var mTableView: UITableView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        req.zipResponse = true
        
        
    }
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        Util.setTabIndex(0)
        let staffID = defaults.integerForKey("staffID")
        
        if staffID > 0 {
            logger.debug("User's staffID: \(staffID)")
            mTableView.rowHeight = 80
            req.requestType = RequestDTO.GET_STAFF_DATA
            req.staffID = staffID
            getCachedData()
        } else {
            logger.debug("User not logged in: start signIn screen")
            
            let sc = self.storyboard?.instantiateViewControllerWithIdentifier("SignInViewController") as! SignInViewController
            self.navigationController?.pushViewController(sc, animated: true)
            
            return
        }
    }
    
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    // MARK: Actions
   
    @IBAction func refreshData(sender: UIBarButtonItem) {
        getRemoteData()
    }
    @IBAction func getRemoteData() {
        showBusy()
        req.zipResponse = false
        req.cacheResponse = true
        let coms = Comms.sharedInstance
        coms.sendRequest(req)
            { (response) -> Void in
            logger.info("Yeeeeeebo! Status Code: \(response.statusCode)")
            self.showBusyOff()
            self.response = response
            self.projectList = response.projectList
            self.mTableView.reloadData()
            //self.cacheData()
            self.title = "\(self.projectList.count) Projects"
        }
    
    }
    func cacheData() {
        
        let filePathToWrite = "\(paths)/data.json"
        let data = response.json.dataUsingEncoding(NSUTF8StringEncoding)
        fileManager.createFileAtPath(filePathToWrite, contents: data, attributes: nil)
        logger.debug("cache data saved")
    }
    func getCachedData() {
        
        response = Util.getCachedData()
        if (response.statusCode == 0) {
            projectList = response.projectList
            mTableView.reloadData()
            let rowToSelect:NSIndexPath = NSIndexPath(forRow: Util.getProjectIndex(), inSection: 0);
            mTableView.scrollToRowAtIndexPath(rowToSelect, atScrollPosition: .Top, animated: true)
            self.title = "\(projectList.count) Projects"
        } else {
            logger.error("Unable to get cached data")
            getRemoteData()
        }
 
    }
    
    func showBusy() {
        indicator.center = view.center
        view.addSubview(indicator)
        indicator.startAnimating()
        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
    }
    func showBusyOff() {
        indicator.stopAnimating()
        UIApplication.sharedApplication().networkActivityIndicatorVisible = false
    }
    
    // MARK: - Table view data source

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return projectList.count
    }

    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("ProjectTableViewCell", forIndexPath: indexPath) as! ProjTableViewCell
        let project = projectList[indexPath.row]

        cell.mProjectName.text = project.projectName
        cell.mPhotoCount.text = "\(project.photoUploadList.count)"
        
        var count = 0
        for projecttask in project.projectTaskList {
            for _ in projecttask.projectTaskStatusList {
                count++
            }
        }
        cell.mStatusCount.text = "\(count)"
        //cell.mImage.hidden = true
        return cell
    }
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        Util.setProjectIndex(indexPath.row)
        selectedProject = projectList[indexPath.row]
        
        let mapController = self.storyboard?.instantiateViewControllerWithIdentifier("ProjectMapViewController") as! ProjectMapViewController
        mapController.project = selectedProject!
        mapController.mapSDK = 2
        
        self.navigationController?.pushViewController(mapController, animated: true)
    }

    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        print("prepareForSegue  \(segue.destinationViewController.description)")
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    

}
