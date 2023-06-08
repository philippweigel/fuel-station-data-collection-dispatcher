package service;

import config.DatabaseConfig;
import model.Station;
import model.StationCustomer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    public List <StationCustomer> fetchAndProcessStationData(int customerID){
        List<StationCustomer> stationCustomers = new ArrayList<>();

        String query = "SELECT * FROM station";
        try(
                PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(query);
                ResultSet rs = ps.executeQuery()
        ){
            while(rs.next()) {
                int id = rs.getInt("id");
                String dbUrl = rs.getString("db_url");
                Float latitude = rs.getFloat("lat");
                Float longitude = rs.getFloat("lng");

                Station station = new Station(id, dbUrl, latitude, longitude);
                StationCustomer stationCustomer = new StationCustomer(station, customerID);
                stationCustomers.add(stationCustomer);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return stationCustomers;

    }

    public int getNumberOfStations() {
        String query = "SELECT COUNT(DISTINCT id) as amount_of_stations FROM station";
        try (
                PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(query);
                ResultSet rs = ps.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("amount_of_stations");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get number of stations");
            e.printStackTrace();
        }
        return 0;
    }
}
