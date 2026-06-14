package org.Gh0st1yAnge1.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.Gh0st1yAnge1.AuditEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class AuditProducer implements AutoCloseable {

    private static final String TOPIC = "audit-log";
    private static final Logger logger = Logger.getLogger(AuditProducer.class.getName());

    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;

    private static final Set<String> AUDITED_COMMANDS = Set.of(
            "INSERT", "UPDATE", "REMOVE_KEY", "REMOVE_GREATER_KEY",
            "REMOVE_GREATER", "REPLACE_IF_LOWER", "CLEAR", "SHOW"
    );

    public static class CommandPartitioner implements Partitioner {

        private static final Set<String> WRITE_COMMANDS = Set.of(
                "INSERT", "UPDATE", "REPLACE_IF_LOWER"
        );

        private static final Set<String> DELETE_COMMANDS = Set.of(
                "REMOVE_KEY", "REMOVE_GREATER_KEY", "REMOVE_GREATER"
        );

        private static final Set<String> OTHER_COMMANDS = Set.of(
                "SHOW", "CLEAR"
        );

        @Override
        public int partition(String topic, Object key, byte[] keyBytes,
                             Object value, byte[] valueBytes, Cluster cluster) {
            String commandType = key instanceof String ? (String) key : "";

            if (WRITE_COMMANDS.contains(commandType))  return 0;
            if (DELETE_COMMANDS.contains(commandType)) return 1;
            if (OTHER_COMMANDS.contains(commandType)) return 2;
            return 0;
        }

        @Override public void close() {}
        @Override public void configure(Map<String, ?> configs) {}
    }

    public AuditProducer(String bootstrapServers) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%6$s%n");
        this.objectMapper = new ObjectMapper();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,   bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CommandPartitioner.class.getName());

        props.put(ProducerConfig.ACKS_CONFIG,          "all");
        props.put(ProducerConfig.RETRIES_CONFIG,        3);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG,   1500);

        this.producer = new KafkaProducer<>(props);
        logger.info("AuditProducer initialized. Bootstrap: " + bootstrapServers);
    }

    public void sendIfAuditable(String commandType, String argument, boolean success, String message) {
        if (!AUDITED_COMMANDS.contains(commandType)) return;

        try {
            AuditEvent event = new AuditEvent(commandType, argument, success, message);
            String json = objectMapper.writeValueAsString(event);

            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, commandType, json);

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    logger.warning("Failed to send audit event: " + exception.getMessage());
                } else {
                    logger.info(String.format(
                            "Audit sent: topic=%s partition=%d offset=%d key=%s",
                            metadata.topic(), metadata.partition(), metadata.offset(), commandType
                    ));
                }
            });
        } catch (Exception e) {
            logger.warning("Audit serialization error: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        producer.flush(); // дожидаемся отправки всего буфера
        producer.close();
        logger.info("AuditProducer closed.");
    }
}