//
//  VCMapView.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/08.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import Foundation
import MapKit
import UIKit

extension ProjectMapViewController: MKMapViewDelegate {
    
    internal func mapView(mapView: MKMapView, viewForAnnotation annotation: MKAnnotation) -> MKAnnotationView? {
        print("MKMapViewDelegate viewForAnnotation")
        if let annotation = annotation as? MapMarker {
            let identifier = "pin"
            var view: MKPinAnnotationView
            if let dequeuedView = mapView.dequeueReusableAnnotationViewWithIdentifier(identifier)
                as? MKPinAnnotationView { // 2
                    dequeuedView.annotation = annotation
                    view = dequeuedView
            } else {
                // 3
                view = MKPinAnnotationView(annotation: annotation, reuseIdentifier: identifier)
                view.canShowCallout = true
                view.calloutOffset = CGPoint(x: -5, y: 5)
                let btn = UIButton(type: .DetailDisclosure)
                //btn.addTarget(self, action: "buttonPressed:", forControlEvents: .TouchUpInside)
                view.leftCalloutAccessoryView = btn
                
            }
            return view
        }
        return nil
    }
    func mapView(mapView: MKMapView, annotationView view: MKAnnotationView,
        calloutAccessoryControlTapped control: UIControl) {
            let location = view.annotation as! MapMarker
            let launchOptions = [MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving]
            location.mapItem().openInMapsWithLaunchOptions(launchOptions)
    }
//    func buttonPressed(sender: UIButton!) {
//        let alertView = UIAlertView();
//        alertView.addButtonWithTitle("OK");
//        alertView.title = "Alert";
//        alertView.message = "Button Pressed!!!";
//        alertView.show();
//    }
//    func mapView(mapView: MKMapView, rendererForOverlay
//        overlay: MKOverlay) -> MKOverlayRenderer {
//            let renderer = MKPolylineRenderer(overlay: overlay)
//            
//            renderer.strokeColor = UIColor.redColor()
//            renderer.lineWidth = 5.0
//            return renderer
//    }
//    func showRoute(response: MKDirectionsResponse) {
//        
//        for route in response.routes {
//            mapView.addOverlay(route.polyline,
//                level: MKOverlayLevel.AboveRoads)
//        }
//    }
    
}
