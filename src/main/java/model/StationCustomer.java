package model;

public class StationCustomer {
    private Station station;
    private int customerId;
    private String uid;

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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "StationCustomer{" +
                "station=" + station +
                ", customerId=" + customerId +
                ", uid='" + uid + '\'' +
                '}';
    }
}
