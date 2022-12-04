package com.offsets;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Properties;

public class AutoConsumer {

    private static final Logger log = LoggerFactory.getLogger(AutoConsumer.class);

    public static void main(String[] args) throws InterruptedException  {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DoubleDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "auto.consumer");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true"); // Default: true
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50); // Default: 500
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000); // Default: 5000
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, Double> consumer = new KafkaConsumer<>(props);

        Thread haltedHook = new Thread(consumer::close);
        Runtime.getRuntime().addShutdownHook(haltedHook);

        consumer.subscribe(Collections.singletonList("auto"));

        while (true) {
            ConsumerRecords<String, Double> records = consumer.poll(Duration.ofMillis(500));
            if (!records.isEmpty()) {
                records.forEach(AutoConsumer::processRecord);
                TopicPartition actualTopicPartition = new TopicPartition("auto", 0);
                final long committedOffset = consumer.committed(Collections.singleton(actualTopicPartition)).get(actualTopicPartition).offset();
                final long consumerCurrentOffset = consumer.position(actualTopicPartition);
                System.out.println("Poll finish.. consumer-offset: " + consumerCurrentOffset + " - committed-offset: " + committedOffset + " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        }
    }

    private static void processRecord(ConsumerRecord record) {
        System.out.printf("\n\n\n New alert received: topic = %s, partition = %d, record offset = %d, key = %s, value %f \n\n\n",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                record.value());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}