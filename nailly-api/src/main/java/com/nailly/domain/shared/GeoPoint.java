package com.nailly.domain.shared;

/** Coordenada geográfica em graus decimais. */
public final class GeoPoint {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final double latitude;
    private final double longitude;

    public GeoPoint(double latitude, double longitude) {
        if (Double.isNaN(latitude) || latitude < -90 || latitude > 90) {
            throw new DomainException("Latitude inválida.");
        }
        if (Double.isNaN(longitude) || longitude < -180 || longitude > 180) {
            throw new DomainException("Longitude inválida.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** Distância em linha reta até outro ponto, em km (fórmula de Haversine). */
    public double distanceKmTo(GeoPoint other) {
        double fromLat = Math.toRadians(latitude);
        double toLat = Math.toRadians(other.latitude);
        double deltaLat = toLat - fromLat;
        double deltaLon = Math.toRadians(other.longitude - longitude);

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(fromLat) * Math.cos(toLat) * Math.pow(Math.sin(deltaLon / 2), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof GeoPoint point
                && Double.compare(latitude, point.latitude) == 0
                && Double.compare(longitude, point.longitude) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(latitude) + Double.hashCode(longitude);
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}
