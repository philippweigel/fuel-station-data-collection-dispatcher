package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import model.Job;
import model.StationCustomer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class RabbitMQService {

    private final static String JOB_START_QUEUE_NAME = "job-start-notifications";
    private final static String DATA_GATHERING_QUEUE_NAME = "create-data-gathering-job";

    private final DatabaseService databaseService;

    private ObjectMapper objectMapper = new ObjectMapper();

    public RabbitMQService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public void consumeMessage(Channel channel, String queueName, DeliverCallback deliverCallback) {
        try {
            channel.queueDeclare(queueName, false, false, false, null);
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            System.err.println("Failed to consume message from queue " + queueName);
            e.printStackTrace();
        }
    }

    public String startJobAndNotifyReceiver(Channel channel) {
        String jobUid = null;
        try {
            jobUid = sendJob(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jobUid;
    }


    private String sendJob(Channel channel) throws IOException {
        channel.queueDeclare(JOB_START_QUEUE_NAME, false, false, false, null);
        int numOfStations = databaseService.getNumberOfStations();
        Job job = new Job();
        String uid = UUID.randomUUID().toString();
        job.setUid(uid);
        job.setExpectedMessageCount(numOfStations);

        String message = objectMapper.writeValueAsString(job);

        channel.basicPublish("", JOB_START_QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
        System.out.println(" [x] Sent'" + message + "'");

        return uid;

    }

    public void sendStationCustomerData(Channel channel, StationCustomer stationCustomer) throws IOException {
        String jsonStationCustomer = objectMapper.writeValueAsString(stationCustomer);
        channel.queueDeclare(DATA_GATHERING_QUEUE_NAME, false, false, false, null);
        channel.basicPublish("", DATA_GATHERING_QUEUE_NAME, null, jsonStationCustomer.getBytes(StandardCharsets.UTF_8));
        System.out.println(" [x] Sent '" + jsonStationCustomer + "'");
    }

}
