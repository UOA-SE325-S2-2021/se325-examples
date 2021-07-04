package se325.example11.parolee.utils;

import se325.example11.parolee.domain.GeoPosition;

public class GeoUtils {

    public static double calculateDistanceInMeters(GeoPosition point1, GeoPosition point2) {
        return haversine(point1.getLatitude(), point1.getLongitude(), point2.getLatitude(), point2.getLongitude());
    }

    // Formula from: movable-type.co.uk/scripts/latlong.html
    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371e3; // metres
        double φ1 = Math.toRadians(lat1);
        double φ2 = Math.toRadians(lat2);
        double Δφ = Math.toRadians(lat2 - lat1);
        double Δλ = Math.toRadians(lon2 - lon1);

        double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                Math.cos(φ1) * Math.cos(φ2) *
                        Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }


}
