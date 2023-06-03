package model;

public class StationCustomer {
    private Station station;
    private int customerId;

    public StationCustomer(Station station, int customerId) {
        this.station = station;
        this.customerId = customerId;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
}
