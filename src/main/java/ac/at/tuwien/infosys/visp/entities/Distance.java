package ac.at.tuwien.infosys.visp.entities;

import java.util.UUID;

public class Distance {
    private String id;
    private String taxiId;
    private String distance;

    public Distance() {
    }

    public Distance(String id, String taxiId, String distance) {
        this.id = id;
        this.taxiId = taxiId;
        this.distance = distance;
    }

    public Distance(String taxiId, String distance) {
        this.id = UUID.randomUUID().toString();
        this.taxiId = taxiId;
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaxiId() {
        return taxiId;
    }

    public void setTaxiId(String taxiId) {
        this.taxiId = taxiId;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
