import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.rabbitmq.client.Connection;

import java.sql.*;


import javax.xml.crypto.Data;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

public class Main {

    private final static String QUEUE_NAME = "create-invoice";

    public static void main(String[] args) {


        try {
            Channel channel = setupRabbitMQChannel();
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
                // Handle the message here.
                int customerID = Integer.parseInt(message);
                fetchAndProcessStationData(customerID, channel);

            };
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }

    private static Channel setupRabbitMQChannel() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(30003);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        return channel;

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
                channel.basicPublish("", QUEUE_NAME, null, jsonStationCustomer .getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + jsonStationCustomer  + "'");
            }
        } catch (SQLException | IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
