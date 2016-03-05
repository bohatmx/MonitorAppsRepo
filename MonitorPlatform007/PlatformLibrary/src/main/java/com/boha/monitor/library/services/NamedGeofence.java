package com.boha.monitor.library.services;

import com.google.android.gms.location.Geofence;

import java.io.Serializable;

/**
 * Created by aubreymalabie on 3/4/16.
 */
public class NamedGeofence implements Serializable {
    String name;
    Double latitude, longitude;
    Float radius;
    Geofence geofence;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Float getRadius() {
        return radius;
    }

    public void setRadius(Float radius) {
        this.radius = radius;
    }

    public Geofence getGeofence() {
        return geofence;
    }

    public void setGeofence(Geofence geofence) {
        this.geofence = geofence;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" - ");
        sb.append("latitude ").append(latitude).append(" longitude ");
        sb.append(longitude).append(" radius ").append(radius);

        return sb.toString();
    }
}
