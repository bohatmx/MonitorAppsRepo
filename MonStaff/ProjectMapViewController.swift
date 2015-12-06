//
//  ProjectMapViewController.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/08.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import MapKit
import UIKit
import GoogleMaps
import Toast_Swift

class ProjectMapViewController: UIViewController, CLLocationManagerDelegate {
    
    
    // MARK: Properties
    //@IBOutlet weak var mLabel: UILabel!
    //@IBOutlet weak var mapView: GMSMapView!
    //@IBOutlet weak var mapView: MKMapView!
    
    @IBOutlet weak var mBarButtonItem: UIBarButtonItem!
    var project:ProjectDTO = ProjectDTO()
    let regionRadius: CLLocationDistance = 1000
    let locationManager = CLLocationManager()
    var mapSDK = 1
    let defaults = NSUserDefaults.standardUserDefaults()
    enum DefaultError: ErrorType {
        case NoSDKFound
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = project.projectName
        print("Project Name: \(project.projectName)")
        
        locationManager.delegate = self
        locationManager.requestWhenInUseAuthorization()
        
        let x =  defaults.valueForKey("mapSDK")
        if (x == nil) {
            mapSDK = 1
            defaults.setInteger(mapSDK, forKey: "mapSDK")
        } else {
            mapSDK = x as! Int
        }
        
        if (mapSDK == 1) {
            showAppleMap()
        } else {
            showGoogleMap()
        }
        
        
    }
    
    func showAppleMap() {
        let mapView = MKMapView()
        
        self.view = mapView
        mapView.delegate = self
        let initialLocation = CLLocation(latitude: project.latitude, longitude: project.longitude)
        centerMapOnLocation(initialLocation, mapView: mapView)
        
        let loc = CLLocationCoordinate2D(latitude: project.latitude, longitude: project.longitude)
        let marker = MapMarker(title: project.projectName, subtitle: "Programme To Be Determined", coordinate: loc)
        mapView.addAnnotation(marker)
    }
    func showGoogleMap() {
        
        let camera = GMSCameraPosition.cameraWithLatitude(project.latitude,
            longitude: project.longitude, zoom: 14)
        let mapView = GMSMapView.mapWithFrame(CGRectZero, camera: camera)
        
        self.view = mapView
        mapView.myLocationEnabled = true
        mapView.settings.myLocationButton = true
        
        let marker = GMSMarker()
        marker.position = CLLocationCoordinate2DMake(project.latitude, project.longitude)
        marker.title = project.projectName
        marker.snippet = "Construction Programme"
        let mapInsets = UIEdgeInsetsMake(100.0, 50.0, 50.0, 50.0)
        mapView.padding = mapInsets
        marker.map = mapView
        
    }
    // MARK: Actions
    
    @IBAction func toggleMapSDK(sender: UIBarButtonItem) {
        
        if (self.mapSDK == 1) {
            self.mapSDK = 2
        } else {
            self.mapSDK = 1
        }
        defaults.setInteger(mapSDK, forKey: "mapSDK")
        if (self.mapSDK == 1) {
            showAppleMap()
        } else {
            showGoogleMap()
        }
        
        
    }
    
    @IBAction func navigateToPhotos(sender: UIBarButtonItem) {
        print("startPhotos .....")
        
        if !findPhotos() {
            print("project has no photos, start alert message")
            let alert = UIAlertController(title: "Project Photos", message: "No Photos have been found. Please hit refresh to find any possible new photos", preferredStyle: UIAlertControllerStyle.Alert)
            alert.addAction(UIAlertAction(title: "Take Project Photo", style: UIAlertActionStyle.Default, handler: { action in
                switch action.style{
                case .Default:
                    print("Take Photo tapped - start camera!")
                    
                case .Cancel:
                    print("cancel")
                    
                case .Destructive:
                    print("destructive")
                }
            }))
//            alert.addAction(UIAlertAction(title: "Done", style: UIAlertActionStyle.Default, handler: { action in
//                switch action.style{
//                case .Default:
//                    print("DONE - default")
//                    
//                case .Cancel:
//                    print("DONE cancel")
//                    
//                case .Destructive:
//                    print("DONE destructive")
//                }
//            }))
            alert.addAction(UIAlertAction(title: "Refresh", style: UIAlertActionStyle.Default, handler: { action in
                switch action.style{
                case .Default:
                    print("Refresh - default")
                    self.getRemoteData()
                    
                case .Cancel:
                    print("Refresh cancel")
                    
                case .Destructive:
                    print("Refresh destructive")
                }
            }))

            self.presentViewController(alert, animated: true, completion: nil)
            return
        }
        let photoController = self.storyboard?.instantiateViewControllerWithIdentifier("PicturesViewController") as! PicturesViewController
        photoController.project = project
        self.navigationController?.pushViewController(photoController, animated: true)
    }
    var response:ResponseDTO = ResponseDTO()
    let fileManager = NSFileManager.defaultManager()
    let paths = NSSearchPathForDirectoriesInDomains(.DocumentDirectory, .UserDomainMask, true)[0] as String
    let req = RequestDTO()
    
    func getRemoteData() {
        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
        
        let staffID = defaults.integerForKey("staffID")
        req.requestType = RequestDTO.GET_STAFF_DATA
        req.staffID = staffID
        req.zipResponse = false
        let coms = Comms.sharedInstance
        coms.sendRequest(req) { (response) -> Void in
            print("\nProjectMapViewController: Yeeeeeebo! status code: \(response.statusCode)")
            self.response = response
            if response.statusCode == 0 {
               self.cacheData()
            } else {
                print("refresh failed, message: \(response.message)")
                
            }
            
            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
        }
        
    }
    func cacheData() {
        
        let filePathToWrite = "\(paths)/data.json"
        let data = response.json.dataUsingEncoding(NSUTF8StringEncoding)
        fileManager.createFileAtPath(filePathToWrite, contents: data, attributes: nil)
        print("cache data saved")
        //replace project with refreshed
        
        for p in response.projectList {
            if (p.projectID == project.projectID) {
                project = p
                print("Project refreshed: \(project.projectName)")
                //let toast = ToastManager.sh
                
                //self.view.makeToast("This is a piece of toast")
                return
            }
        }
        
    }

    func findPhotos() -> Bool {
        if (!project.photoUploadList.isEmpty) {
            return true
        }
        
        return false;
    }
    
    func centerMapOnLocation(location: CLLocation, mapView:MKMapView) {
        let coordinateRegion = MKCoordinateRegionMakeWithDistance(location.coordinate,
            regionRadius * 2.0, regionRadius * 2.0)
        mapView.setRegion(coordinateRegion, animated: true)
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    var location:CLLocation?
    
    /*
    // MARK: - Navigation
    
    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
    // Get the new view controller using segue.destinationViewController.
    // Pass the selected object to the new view controller.
    }
    */
    
    // MARK: - CLLocationManagerDelegate
    func locationManager(manager: CLLocationManager, didChangeAuthorizationStatus status: CLAuthorizationStatus) {
        print("****** didChangeAuthorizationStatus \(status)")
        if status == .AuthorizedWhenInUse {
            locationManager.startUpdatingLocation()
        }
    }
    
    func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        
        self.location = locations.first
        
        locationManager.stopUpdatingLocation()
        
        print("\n##### didUpdateLocations: \(locations.count) \(self.location)")
        
    }
}

