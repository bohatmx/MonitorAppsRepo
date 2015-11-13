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
        let photoController = self.storyboard?.instantiateViewControllerWithIdentifier("PicturesViewController") as! PicturesViewController
        photoController.project = project
        self.navigationController?.pushViewController(photoController, animated: true)
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

