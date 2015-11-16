package ac.at.tuwien.infosys.visp.entities;

import java.util.UUID;

public class Report {
    private String id;
    private String averageSpeed;
    private String distance;
    private String taxiId;

    public Report(String id, String taxiId, String averageSpeed, String distance) {
        this.id = id;
        this.taxiId = taxiId;
        this.averageSpeed = averageSpeed;
        this.distance = distance;
    }

    public Report() {
    }

    public Report(String taxiId, String averageSpeed, String distance) {
        this.id = UUID.randomUUID().toString();
        this.averageSpeed = averageSpeed;
        this.distance = distance;
        this.taxiId = taxiId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(String averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTaxiId() {
        return taxiId;
    }

    public void setTaxiId(String taxiId) {
        this.taxiId = taxiId;
    }
}
