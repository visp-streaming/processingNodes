package ac.at.tuwien.infosys.visp.entities;

public class Location {
    private String id;
    private String taxiId;
    private String time;
    private String latitude;
    private String longitude;

    public Location() {
    }

    public Location(String id, String taxiId, String time, String latitude, String longitude) {

        this.id = id;
        this.taxiId = taxiId;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
