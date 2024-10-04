package com.example.adoptr_backend.util;

import com.example.adoptr_backend.model.Location;
import com.example.adoptr_backend.service.dto.response.LostDTO;

public class DistanceHelper {
    public static double calculateDistance(LostDTO lost, Location location) {
        double latitudeEntity = Math.toRadians(lost.getLatitude());
        double longitudeEntity = Math.toRadians(lost.getLongitude());

        double latitudeParameter = Math.toRadians(location.getLatitude());
        double longitudeParameter = Math.toRadians(location.getLongitude());

        // Haversine formula
        double distanceLongitude = longitudeParameter - longitudeEntity;
        double distanceLatitude = latitudeParameter - latitudeEntity;

        double a = Math.pow(Math.sin(distanceLatitude / 2), 2)
                + Math.cos(latitudeParameter) * Math.cos(latitudeEntity)
                * Math.pow(Math.sin(distanceLongitude / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        return (c * r);
    }
}
