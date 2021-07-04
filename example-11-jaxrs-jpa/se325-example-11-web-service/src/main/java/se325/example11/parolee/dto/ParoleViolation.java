package se325.example11.parolee.dto;

import se325.example11.parolee.domain.GeoPosition;

public class ParoleViolation {

    private long paroleeId;

    private GeoPosition location;

    public ParoleViolation() {
    }

    public ParoleViolation(long paroleeId, GeoPosition location) {
        this.paroleeId = paroleeId;
        this.location = location;
    }

    public long getParoleeId() {
        return paroleeId;
    }

    public void setParoleeId(long paroleeId) {
        this.paroleeId = paroleeId;
    }

    public GeoPosition getLocation() {
        return location;
    }

    public void setLocation(GeoPosition location) {
        this.location = location;
    }
}
