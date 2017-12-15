package hangxu.finalproject.cs5520.hikerplus.model;

import java.util.List;

/**
 * Model Class representing a user's hiking record.
 */

public class Record {

    private String date;
    private String destinationAddress;
    private List<MLatLng> routePoints;
    private String timeSpan;
    private String stepNum;
    private double distance;
    private String recordId;
    private MLatLng startLocation;
    private MLatLng stopLocation;

    public Record() {

    }

    public Record(String date, String destinationAddress, List<MLatLng> routePoints, String timeSpan,
                  String stepNum, double distance, String recordId, MLatLng startLocation, MLatLng stopLocation) {
        this.date = date;
        this.destinationAddress = destinationAddress;
        this.routePoints = routePoints;
        this.timeSpan = timeSpan;
        this.stepNum = stepNum;
        this.distance = distance;
        this.recordId = recordId;
        this.startLocation = startLocation;
        this.stopLocation = stopLocation;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setRoutePoints(List<MLatLng> routePoints) {
        this.routePoints = routePoints;
    }

    public List<MLatLng> getRoutePoints() {
        return routePoints;
    }

    public void setTimeSpan(String timeSpan) {
        this.timeSpan = timeSpan;
    }

    public String getTimeSpan() {
        return timeSpan;
    }

    public void setStepNum(String stepNum) {
        this.stepNum = stepNum;
    }

    public String getStepNum() {
        return stepNum;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setStartLocation(MLatLng startLocation) {
        this.startLocation = startLocation;
    }

    public MLatLng getStartLocation() {
        return startLocation;
    }

    public void setStopLocation(MLatLng stopLocation) {
        this.stopLocation = stopLocation;
    }

    public MLatLng getStopLocation() {
        return stopLocation;
    }
}
