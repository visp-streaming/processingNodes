package ac.at.tuwien.infosys.visp.entities;


import java.util.List;

public class Locations {

    private List<Location> locations;

    public Locations(List<Location> locations) {
        this.locations = locations;
    }

    public Locations() {
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }
}
