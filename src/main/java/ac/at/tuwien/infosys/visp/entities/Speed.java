package ac.at.tuwien.infosys.visp.entities;

import java.util.UUID;

public class Speed {
    private String id;
    private String taxiId;
    private String speed;

    public Speed() {
    }

    public Speed(String id, String taxiId, String speed) {

        this.id = id;
        this.taxiId = taxiId;
        this.speed = speed;
    }

    public Speed(String taxiId, String speed) {
        this.id = UUID.randomUUID().toString();
        this.taxiId = taxiId;
        this.speed = speed;
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

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }
}
