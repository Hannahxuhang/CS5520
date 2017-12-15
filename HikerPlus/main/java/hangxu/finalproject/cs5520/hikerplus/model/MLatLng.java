package hangxu.finalproject.cs5520.hikerplus.model;

/**
 * Model class used for replacing LatLng of Google Services because of no public constructor.
 */

public class MLatLng {
    private double latitude;
    private double longitude;

    public MLatLng() {

    }

    public MLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
