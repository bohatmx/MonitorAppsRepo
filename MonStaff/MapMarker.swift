//
//  MapMarker.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/08.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import Foundation
import MapKit
import AddressBook

public class MapMarker: NSObject, MKAnnotation {
    public var title: String?
    public var subtitle: String?
    public var coordinate: CLLocationCoordinate2D
    
    init(title: String, subtitle: String, coordinate: CLLocationCoordinate2D) {
        self.title = title
        self.subtitle = subtitle
        self.coordinate = coordinate
        super.init()
    }
    func mapItem() -> MKMapItem {
        let pm = MKPlacemark(coordinate: coordinate, addressDictionary: nil)        
        let mapItem = MKMapItem(placemark: pm)
        mapItem.name = title
        
        return mapItem
    }
}
