import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.rabbitmq.client.Connection;
import config.Database;
import config.RabbitMQConfiguration;
import model.Station;
import model.StationCustomer;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

public class Main {

    private final static String CREATE_INVOICE_QUEUE_NAME = "create-invoice";
    private final static String DATA_GATHERING_QUEUE_NAME = "create-data-gathering-job";

    public static void main(String[] args) {


        try {
            Channel channel = RabbitMQConfiguration.setupRabbitMQChannel();
            channel.queueDeclare(CREATE_INVOICE_QUEUE_NAME, false, false, false, null);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
                // Handle the message here.
                int customerID = Integer.parseInt(message);
                fetchAndProcessStationData(customerID, channel);

            };
            channel.basicConsume(CREATE_INVOICE_QUEUE_NAME, true, deliverCallback, consumerTag -> { });
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }

    private static void fetchAndProcessStationData(int customerID, Channel channel){
        String query = "SELECT * FROM station";
        ObjectMapper objectMapper = new ObjectMapper();

        try(
                PreparedStatement ps = Database.getConnection().prepareStatement(query);
                ResultSet rs = ps.executeQuery()
        ){
            while(rs.next()) {
                int id = rs.getInt("id");
                String dbUrl = rs.getString("db_url");
                Float latitude = rs.getFloat("lat");
                Float longitude = rs.getFloat("lng");

                Station station = new Station(id, dbUrl, latitude, longitude);
                StationCustomer stationCustomer = new StationCustomer(station, customerID);

                // Serialize jsonStationCustomer to JSON and send the JSON string to RabbitMQ.
                String jsonStationCustomer  = objectMapper.writeValueAsString(stationCustomer);
                channel.basicPublish("", DATA_GATHERING_QUEUE_NAME, null, jsonStationCustomer .getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + jsonStationCustomer  + "'");
            }
        } catch (SQLException | IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
