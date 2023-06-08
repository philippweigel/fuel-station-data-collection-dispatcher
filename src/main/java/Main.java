import com.rabbitmq.client.*;
import config.RabbitMQConfig;
import model.StationCustomer;
import service.RabbitMQService;
import service.DatabaseService;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Main {

    private final static String CREATE_INVOICE_QUEUE_NAME = "create-invoice";


    public static void main(String[] args) {
        DatabaseService databaseService = new DatabaseService();
        RabbitMQService rabbitMQService = new RabbitMQService(databaseService);

        Channel channel;

        try {
            channel = RabbitMQConfig.setupRabbitMQChannel();
            rabbitMQService.consumeMessage(channel, CREATE_INVOICE_QUEUE_NAME, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");

                int customerID = Integer.parseInt(message);
                List<StationCustomer> stationCustomers = databaseService.fetchAndProcessStationData(customerID);
                String jobUid = rabbitMQService.startJobAndNotifyReceiver(channel);
                if (jobUid != null) {
                    for (StationCustomer stationCustomer : stationCustomers) {
                        stationCustomer.setUid(jobUid);
                        rabbitMQService.sendStationCustomerData(channel, stationCustomer);
                    }
                }
            });
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }



}
