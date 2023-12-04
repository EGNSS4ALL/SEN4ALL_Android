package org.erasmicoin.euspa.sen4all;

import java.util.ArrayList;

public class GeocodingResult {
    String displayName;
    String addressType;

    Coordinate coordinate;
    ArrayList<Coordinate> boundingBox;

    String errorMessage;

    boolean status;

    public GeocodingResult(boolean status) {
        this.status = status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public ArrayList<Coordinate> getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(ArrayList<Coordinate> boundingBox) {
        this.boundingBox = boundingBox;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
