package org.Gh0st1yAnge1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.sql.*;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class AuditConsumerApp {

    private static final String TOPIC   = "audit-log";
    private static final Logger logger  = Logger.getLogger(AuditConsumerApp.class.getName());

    private static final String DB_CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS audit_log (
                id           INTEGER PRIMARY KEY AUTOINCREMENT,
                command_type TEXT    NOT NULL,
                argument     TEXT,
                timestamp    TEXT    NOT NULL,
                success      INTEGER NOT NULL,
                message      TEXT,
                partition_id INTEGER NOT NULL,
                created_at   TEXT    DEFAULT (datetime('now'))
            )
            """;

    private static final String DB_INSERT = """
            INSERT INTO audit_log (command_type, argument, timestamp, success, message, partition_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%6$s%n");
        String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";
        String ownDbPath        = args.length > 1 ? args[1] : "./audit.db";
        int    partitionId      = args.length > 2 ? Integer.parseInt(args[2]) : 0;
        String sharedDbPath     = args.length > 3 ? args[3] : "./audit-all.db";

        logger.info(String.format(
                "Starting AuditConsumer: kafka=%s ownDb=%s partition=%d sharedDb=%s",
                bootstrapServers, ownDbPath, partitionId, sharedDbPath
        ));

        try (Connection ownConn    = openDatabase(ownDbPath);
             Connection sharedConn = openDatabase(sharedDbPath)) {

            initDatabase(ownConn);
            initDatabase(sharedConn);
            logger.info("Databases ready.");

            runConsumer(bootstrapServers, ownConn, sharedConn, partitionId);

        } catch (SQLException e) {
            logger.severe("Database error: " + e.getMessage());
        }
    }

    private static void runConsumer(String bootstrapServers,Connection ownConn,Connection sharedConn,int partitionId) {
        ObjectMapper objectMapper = new ObjectMapper();

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,        bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "audit-consumer-partition-" + partitionId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,        "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,       "false");

        final KafkaConsumer<String, String>[] ref = new KafkaConsumer[1];
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal. Waking up consumer...");
            if (ref[0] != null) ref[0].wakeup();
        }));

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            ref[0] = consumer;

            TopicPartition tp = new TopicPartition(TOPIC, partitionId);
            consumer.assign(List.of(tp));
            logger.info("Assigned to partition: " + partitionId);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(200));
                if (records.isEmpty()) continue;

                int saved = 0;
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        AuditEvent event = objectMapper.readValue(record.value(), AuditEvent.class);

                        saveToDatabase(ownConn, event, record.partition());
                        saveToDatabase(sharedConn, event, record.partition());
                        saved++;

                        logger.info(String.format(
                                "Sent [%s] -> partition=%d offset=%d",
                                event.getCommandType(), record.partition(), record.offset()

                        ));

                    } catch (Exception e) {
                        logger.warning("Failed at offset " + record.offset() + ": " + e.getMessage());
                    }
                }

                consumer.commitSync();
                if (saved > 0) {
                    logger.info(String.format("Committed %d records (partition %d)", saved, partitionId));
                }
            }

        } catch (org.apache.kafka.common.errors.WakeupException e) {
            logger.info("Consumer stopped by wakeup.");
        }

        logger.info("AuditConsumer shutdown complete.");
    }

    private static Connection openDatabase(String dbPath) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("PRAGMA busy_timeout=1500");
        }
        return conn;
    }

    private static void initDatabase(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(DB_CREATE_TABLE);
        }
    }

    private static void saveToDatabase(Connection conn, AuditEvent event, int partitionId)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DB_INSERT)) {
            ps.setString(1, event.getCommandType());
            ps.setString(2, event.getArgument());
            ps.setString(3, event.getTimestamp());
            ps.setInt   (4, event.isSuccess() ? 1 : 0);
            ps.setString(5, event.getMessage());
            ps.setInt   (6, partitionId);
            ps.executeUpdate();
        }
    }
}