package org.Gh0st1yAnge1;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
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
}package org.Gh0st1yAnge1;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
package org.Gh0st1yAnge1.utils;

public class Validator {

    public static boolean validateRouteName(String name){
        return !name.isEmpty();
    }

    public static boolean validateCoordinateX(Float x){
        return x > -720f;
    }

    public static boolean validateCoordinateY(float y){
        return y <= 650;
    }

    public static boolean validateLocationZ(Integer z){
        return z != null;
    }

    public static boolean validateRouteDistance(float distance){
        return distance > 1f;
    }

    public static boolean validateLocationType(Integer type){return type == 1 || type == 2;}

}
package org.Gh0st1yAnge1.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class TcpUtil {
    public static final int CHUNK_SIZE = 8 * 1024;

    private TcpUtil(){};

    public static Path serializeToTempFile(Object object) throws IOException {
        Path tempFile = Files.createTempFile("tcp-payload", ".bin");
        try (OutputStream outputStream = Files.newOutputStream(tempFile);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
        }
        return tempFile;
    }

    public static Object desirealizeFromFile(Path filePath) throws IOException, ClassNotFoundException{
        try (InputStream inputStream = Files.newInputStream(filePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)){
            return objectInputStream.readObject();
        }
    }

    public static void writeChunkedFromFile(SocketChannel socketChannel, Path filePath) throws IOException{
        try (Selector writeSelector = Selector.open()){
            socketChannel.register(writeSelector, SelectionKey.OP_WRITE);

            ByteBuffer headerBuf = ByteBuffer.allocate(4);
            byte[] chunk = new byte[CHUNK_SIZE];

            try (InputStream inputStream = Files.newInputStream(filePath)){
                int read;
                while ((read = inputStream.read(chunk)) != -1){
                    headerBuf.clear();
                    headerBuf.putInt(read);
                    headerBuf.flip();
                    writeFully(socketChannel, headerBuf, writeSelector);

                    ByteBuffer payloadBuf = ByteBuffer.wrap(chunk, 0, read);
                    writeFully(socketChannel, payloadBuf, writeSelector);
                }

                headerBuf.clear();
                headerBuf.putInt(0);
                headerBuf.flip();
                writeFully(socketChannel, headerBuf, writeSelector);
            }
        }
    }

    private static void writeFully(SocketChannel socketChannel, ByteBuffer byteBuffer, Selector selector) throws IOException {
        while (byteBuffer.hasRemaining()){
            int written = socketChannel.write(byteBuffer);
            if (written < 0){
                throw new EOFException("Channel closed while writing.");
            }
            if (written == 0){
                selector.select();
                selector.selectedKeys().clear();
            }
        }
    }
}

package org.Gh0st1yAnge1;

import java.io.Serializable;
import java.time.Instant;

public class AuditEvent implements Serializable {

    private String commandType;
    private String argument;
    private String timestamp;
    private boolean success;
    private String message;

    public AuditEvent() {}

    public AuditEvent(String commandType, String argument, boolean success, String message) {
        this.commandType = commandType;
        this.argument    = argument;
        this.timestamp   = Instant.now().toString();
        this.success     = success;
        this.message     = message;
    }

    public String getCommandType() { return commandType; }
    public void setCommandType(String commandType) { this.commandType = commandType; }

    public String getArgument() { return argument; }
    public void setArgument(String argument) { this.argument = argument; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "AuditEvent{commandType='" + commandType + "', argument='" + argument +
               "', timestamp='" + timestamp + "', success=" + success + '}';
    }
}
package org.Gh0st1yAnge1.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

public class Route implements Comparable<Route>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long key;
    private String name;
    private Coordinates coordinates;
    private java.time.LocalDate creationDate;
    private Location from;
    private Location to;
    private float distance;
    private Long ownerId;

    public Route() {}

    public Route(String name, Coordinates coordinates, Location from, Location to, float distance){
        this.key = null;
        this.ownerId = null;
        this.name = name;
        this.coordinates = coordinates;
        this.to = to;
        this.from = from;
        this.distance = distance;
        this.creationDate = LocalDate.now();
    }

    @Override
    public int compareTo(Route other) {
        if (this.to == null && other.getTo() == null) return 0;
        if (this.to == null) return 1;
        if (other.getTo() == null) return -1;
        return this.to.compareTo(other.getTo());
    }

    public void setKey(Long id) {this.key = key; }
    public Long getKey() {
        return key;
    }

    public void setOwnerId(Long id) {this.ownerId = ownerId; }
    public Long getOwnerId() { return  ownerId; }

    public void setName(String name) {
            this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setCoordinates(Coordinates coordinates) {
            this.coordinates = coordinates;
    }
    public Coordinates getCoordinates() {
        return coordinates;
    }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }

    public void setFrom(Location from) {
            this.from = from;
    }
    public Location getFrom() {
        return from;
    }

    public void setTo(Location to) {
            this.to = to;
    }
    public Location getTo() {
        return to;
    }

    public void setDistance(float distance) {
            this.distance = distance;
    }
    public float getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return "Route{" +
                "key=" + key +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", from=" + from +
                ", to=" + to +
                ", distance=" + distance +
                ", ownerId=" + ownerId +
                '}';
    }
}package org.Gh0st1yAnge1.model;

import java.io.Serial;
import java.io.Serializable;

public class Coordinates implements Comparable<Coordinates>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Float x;
    private float y;

    public Coordinates(Float x, float y){
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Coordinates other) {
        return this.x.compareTo(other.getX());
    }

    public void setX(Float x) {
            this.x = x;
    }
    public Float getX() {
        return x;
    }

    public void setY(float y) {
            this.y = y;
    }
    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Coordinates{\n" +
                "        x=" + x +
                ",\n        y=" + y + '\n' +
                "    }";
    }

}
package org.Gh0st1yAnge1.model;

import java.io.Serial;
import java.io.Serializable;

public class Location implements Comparable<Location>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private double x;
    private double y;
    private Integer z;
    private String name;

    //without name
    public Location(double x, double y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = null;
    }

    //with name
    public Location(double x, double y, int z, String name){
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    @Override
    public int compareTo(Location other) {
        if (this.name == null && other.getName() == null) return 0;
        if (this.name == null) return 1;
        if (other.getName() == null) return -1;
        return this.name.compareTo(other.getName());
    }

    public void setX(Double x) {
        this.x = x;
    }
    public double getX() {
        return x;
    }

    public void setY(Double intY) { this.y = y;}
    public double getIntY() {
        return y;
    }

    public void setZ(Integer intZ) {
        this.z = z;
    }
    public Integer getIntZ() {
        return z;
    }

    public void setName(String name) {
            this.name = name;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", name='" + name + '\'' +
                '}';
    }
}
package org.Gh0st1yAnge1.exceptions;

public class InputCancelledException extends RuntimeException {
    public InputCancelledException(String message) {
        super(message);
    }
}
package org.Gh0st1yAnge1.request_and_response;

import org.Gh0st1yAnge1.model.Route;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record Response(boolean success, String message, List<Route> collection)  implements Serializable {

    public Response(boolean success, String message) {
        this(success, message, null);
    }

    @Serial
    private static final long serialVersionUID = 1L;
}
package org.Gh0st1yAnge1.request_and_response;


public enum CommandType {
    AVERAGE_OF_DISTANCE, CLEAR, COUNT_BY_DISTANCE, EXECUTE_SCRIPT,
    EXIT, FILTER_LESS_THAN_DISTANCE, HELP, INFO,
    INSERT, REMOVE_GREATER, REMOVE_GREATER_KEY, REMOVE_KEY,
    REPLACE_IF_LOWER, SHOW, UPDATE, SAVE_SERVER, CHECK_INSERT_KEY, CHECK_UPDATE_KEY, HELP_SERVER,
    LOGIN, REGISTER, LOGOUT;
}package org.Gh0st1yAnge1.request_and_response;

import org.Gh0st1yAnge1.model.Route;
import java.io.Serial;
import java.io.Serializable;

public record Request(
        CommandType commandType,
        String argument,
        Route route,
        String login,
        String password) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public Request (CommandType commandType, String argument, Route route) {
        this (commandType, argument, route, null, null);
    }

    public boolean isAuthenticated() {
        return login != null && !login.trim().isEmpty()
                && password != null && !password.trim().isEmpty();
    }
}
package org.Gh0st1yAnge1;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
package org.Gh0st1yAnge1.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {
    private static final Logger logger = Logger.getLogger("ServerLogger");

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleFormatter());

        logger.addHandler(handler);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
    }

    public static Logger getLogger(){
        return logger;
    }

    public static void logInfo(String message){
        logger.info(message);
    }

    public static void logWarning(String message){
        logger.warning(message);
    }

    public static void logSevere(String message){
        logger.severe(message);
    }

    public static void logFine(String message){
        logger.fine(message);
    }
}
package org.Gh0st1yAnge1.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;

public class LocalDateAdapter extends TypeAdapter<LocalDate> {

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.value(value.toString());
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {

        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        return LocalDate.parse(in.nextString());
    }
}package org.Gh0st1yAnge1.utils;

public class IdGenerator {

    private static int currentMaxId = 0;

    public static int generate(){
        currentMaxId++;
        return currentMaxId;
    }

    public static void compareMaxId(int id){
        if (id > currentMaxId){
            currentMaxId = id;
        }
    }

    public static void reset(){
        currentMaxId = 0;
    }
}
package org.Gh0st1yAnge1.server_commands;

public class Exit implements ServerCommand {

    public Exit(){}

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getDescription() {
        return "terimantes session";
    }

    @Override
    public String execute(String arg) {
        return "Session terminated.";
    }
}
package org.Gh0st1yAnge1.server_commands;

import org.Gh0st1yAnge1.manager.ServerCommandExecutor;

public class ServerHelp implements ServerCommand{

    private final ServerCommandExecutor serverCommandExecutor;

    public ServerHelp(ServerCommandExecutor serverCommandExecutor){this.serverCommandExecutor = serverCommandExecutor;}

    @Override
    public String getName() {
        return "help_server";
    }

    @Override
    public String getDescription() {
        return "shows available commands";
    }

    @Override
    public String execute(String arg) {
        String answer = "Available commands:\n";
        for (ServerCommand serverCommand: serverCommandExecutor.getServerCommands().values()){
            answer += "\n";
            answer += "--" + serverCommand.getName() + "--\n";
            answer += serverCommand.getDescription()+ "\n";
        }
        return answer;
    }
}package org.Gh0st1yAnge1.server_commands;

import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.manager.FileManager;
import org.Gh0st1yAnge1.model.Route;

import java.util.LinkedHashMap;

public class SaveWithPath implements ServerCommand{

    private final CollectionManager collectionManager;

    public SaveWithPath (CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "save_with_path";
    }

    @Override
    public String getDescription() {
        return "saves collection using inputted path";
    }

    @Override
    public String execute(String arg) {
        String path = (arg != null) ? arg : "backup.json";
        FileManager backupManager = new FileManager(path);
        LinkedHashMap<Integer, Route> snapshot = new LinkedHashMap<>(collectionManager.getMap());
        return backupManager.saveCollection(snapshot);
    }
}
package org.Gh0st1yAnge1.server_commands;

public interface ServerCommand {
    String getName();
    String getDescription();
    String execute(String arg);
}
package org.Gh0st1yAnge1.auth;

import org.Gh0st1yAnge1.auth.algorithms.*;

public class HasherFactory {

     public static PasswordHasher create(String algorithm) {
         if (algorithm == null){
             return new DigestHasher("SHA-256");
         }

         String algo = algorithm.toUpperCase();

         return switch (algo) {
             case "BCRYPT" -> new BCryptHasher();
             case "SCRYPT" -> new SCryptHasher();
             case "ARGON2" -> new Argon2Hasher();
             default -> new DigestHasher(algo);
         };

     }
}
package org.Gh0st1yAnge1.auth;

public interface PasswordHasher {
    String hash(String password);
    boolean verify(String password, String hash);
}
package org.Gh0st1yAnge1.auth;

import org.Gh0st1yAnge1.db.UserRepository;

import java.sql.SQLException;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String register(String username, String password) {
        try {
            boolean success = userRepository.register(username, password);
            return success ? "User '" + username + "' is registered!" : "Username's already exists!";
        } catch (SQLException e) {
            return "Registration error: " + e.getMessage();
        }
    }

    public Long login(String username, String password) throws SQLException {
            return userRepository.authenticate(username, password);
    }
}
package org.Gh0st1yAnge1.auth.algorithms;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.Gh0st1yAnge1.auth.PasswordHasher;

public class Argon2Hasher implements PasswordHasher {

    private final Argon2 argon2 = Argon2Factory.create();

    @Override
    public String hash(String password) {
        return argon2.hash(10, 65536, 1,password.toCharArray());
    }

    @Override
    public boolean verify(String password, String hash) {
        return argon2.verify(hash, password.toCharArray());
    }
}
package org.Gh0st1yAnge1.auth.algorithms;

import com.lambdaworks.crypto.SCryptUtil;
import org.Gh0st1yAnge1.auth.PasswordHasher;

public class SCryptHasher implements PasswordHasher {

    private static final int N = 16384;
    private static final int r = 8;
    private static final int p = 1;

    @Override
    public String hash(String password) {
        return SCryptUtil.scrypt(password, N, r, p);
    }

    @Override
    public boolean verify(String password, String hash) {
        return SCryptUtil.check(password, hash);
    }
}
package org.Gh0st1yAnge1.auth.algorithms;

import org.Gh0st1yAnge1.auth.PasswordHasher;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestHasher implements PasswordHasher {

    private final String algorithm;

    public DigestHasher(String algorithm){
        this.algorithm = algorithm;
    }

    @Override
    public String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(algorithm + " isn't supported. " + e.getMessage());
        }
    }

    @Override
    public boolean verify(String password, String hash) {
        return hash(password).equals(hash);
    }

    private String bytesToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes){
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
package org.Gh0st1yAnge1.auth.algorithms;

import org.Gh0st1yAnge1.auth.PasswordHasher;
import org.mindrot.jbcrypt.BCrypt;

public class BCryptHasher implements PasswordHasher {

    private static final int ROUNDS = 10;

    @Override
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(ROUNDS));
    }

    @Override
    public boolean verify(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}
package org.Gh0st1yAnge1.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private final Properties props = new Properties();

    public Config() throws IOException {
        File configFile = new File("application.properties");
        if (configFile.exists()){
            try (InputStream input = new FileInputStream(configFile)){
                props.load(input);
                System.out.println("Config from application.properties is loaded.");
            }
        } else {
            System.out.println("File application properties isn't found. Use environmental variables.");
        }
        overwriteFromEnv();
    }

    private void overwriteFromEnv() {
        setIfEnvExists("db.url", "DB_URL");
        setIfEnvExists("db.user", "DB_USER");
        setIfEnvExists("db.password", "DB_PASS");
        setIfEnvExists("kafka.servers", "KAFKA_BOOTSTRAP_SERVERS");

        setIfEnvExists("hash.algorithm", "HASH_ALGORITHM");
        setIfEnvExists("read.executor", "READ_EXECUTOR");
        setIfEnvExists("process.executor", "PROCESS_EXECUTOR");
        setIfEnvExists("send.executor", "SEND_EXECUTOR");
        setIfEnvExists("sync.strategy", "SYNC_STRATEGY");
    }

    private void setIfEnvExists (String propKey, String envKey) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            props.setProperty(propKey, envValue);
            System.out.println("Overwritten " + propKey + " from " + envKey);
        }
    }

    public String getDbUrl() {
        String url = props.getProperty("db.url");
        if (url == null) throw new IllegalStateException("db.url isn't set");
        return url;
    }

    public String getDbUser() {
        String user = props.getProperty("db.user");
        if (user == null) throw new IllegalStateException("db.user isn't set");
        return user;
    }

    public String getDbPassword() {
        String pass = props.getProperty("db.password");
        if (pass == null) throw new IllegalStateException("db.password isn't set");
        return pass;
    }

    public String getKafkaBootstrapServers (){
        return props.getProperty("kafka.servers", "localhost:9092");
    }

    public String getHashAlgorithm() {
        return props.getProperty("hash.algorithm", "SHA-256");
    }

    public String getReadExecutor() {
        return props.getProperty("read.executor", "thread");
    }

    public String getProcessExecutor() {
        return props.getProperty("process.executor", "thread");
    }

    public String getSendExecutor() {
        return props.getProperty("send.executor", "thread");
    }

    public String getSyncStrategy() {
        return props.getProperty("sync.strategy", "readwritelock");
    }

    public void printConfig() {
        System.out.println("\n========== Current Configuration ==========");
        System.out.println("DB URL: " + getDbUrl());
        System.out.println("DB User: " + getDbUser());
        System.out.println("DB Password: " + (getDbPassword().isEmpty() ? "Not set" : "Set!"));
        System.out.println("Hash Algorithm: " + getHashAlgorithm());
        System.out.println("Read Executor: " + getReadExecutor());
        System.out.println("Process Executor: " + getProcessExecutor());
        System.out.println("Send Executor: " + getSendExecutor());
        System.out.println("Sync Strategy: " + getSyncStrategy());
        System.out.println("Kafka bootstrap servers: " + getKafkaBootstrapServers());
        System.out.println("==========================================\n");
    }
}
package org.Gh0st1yAnge1.client_commands.auth;

import org.Gh0st1yAnge1.auth.AuthService;
import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public class Register implements ClientCommand {

    private final AuthService authService;

    public Register(AuthService authService){
        this.authService = authService;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {
        if (arg == null || arg.trim().isEmpty()) {
            return new Response(false, "Usage: register <login> <password>");
        }
        String[] parts = arg.trim().split("\\s+");
        if (parts.length != 2){
            return new Response(false, "Usage: register <login> <password>");
        }

        String result = authService.register(parts[0], parts[1]);

        boolean success = !result.toLowerCase().contains("already")
                && !result.toLowerCase().contains("error")
                && !result.toLowerCase().contains("fail");
        return new Response(success, result);
    }

    @Override
    public String getName() {
        return "register";
    }

    @Override
    public String getDescription() {
        return "register <login> <password> — create a new account\"";
    }
}
package org.Gh0st1yAnge1.client_commands.auth;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public class Logout implements ClientCommand {

    @Override
    public Response execute(String arg, Route route, Long userId) {
        return new Response(true, "Logged out!");
    }

    @Override
    public String getName() {
        return "logout";
    }

    @Override
    public String getDescription() {
        return "ends your current session";
    }
}
package org.Gh0st1yAnge1.client_commands.auth;

import org.Gh0st1yAnge1.auth.AuthService;
import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class Login implements ClientCommand {

    private final AuthService authService;

    public Login(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {
        if (arg == null || arg.trim().isEmpty()) {
            return new Response(false, "Usage: login <login> <password>");
        }
        String[] parts = arg.trim().split("\\s+");
        if (parts.length != 2) {
            return new Response(false, "Usage: login <login> <password>");
        }

        try {
            // Вызываем метод, который теперь может выбросить SQLException
            Long authenticatedId = authService.login(parts[0], parts[1]);

            if (authenticatedId != null) {
                return new Response(true, "Login successful! Welcome, " + parts[0] + ".");
            }
            return new Response(false, "Invalid login or password.");

        } catch (SQLException e) {
            // Здесь мы изящно ловим падение базы/туннеля прямо в момент первой попытки входа
            return new Response(false, "Database error: Cannot authenticate right now. Server database is unavailable.");
        }
    }

    @Override
    public String getName()        { return "login"; }
    @Override
    public String getDescription() { return "login <login> <password> — authenticate"; }
}package org.Gh0st1yAnge1.client_commands;

import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public interface ClientCommand {
    String getName();
    String getDescription();
    Response execute(String arg, Route route, Long userId);
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class Clear implements ClientCommand {

    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public Clear(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg != null){
            return new Response(false, "Usage: clear");
        }

        try {
            routeRepository.clear(userId);
            collectionManager.clearOnly(userId);
            return new Response(true, "Successfully cleared!");
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "clears collection";
    }
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class RemoveGreater implements ClientCommand {

    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public RemoveGreater(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg != null){
            return new Response(false, "Usage: remove_greater");
        }

        if (route == null){
            return new Response(false, "Route must not be null");
        }

        try {
            int removedRoutes = routeRepository.removeGreater(route, userId);
            if (removedRoutes > 0){
                collectionManager.removeGreater(route, userId);
                return  new Response(true, "Successfully removed!" + "Number of removed elements: " + removedRoutes);
            }
            return new Response(true, "Collection size didn't change.");
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "remove_greater";
    }

    @Override
    public String getDescription() {
        return "removes all collection elements,\nwho is more than inserted";
    }
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class RemoveKey implements ClientCommand {

    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public RemoveKey(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userid) {

        if (arg == null || arg.isBlank()){
            return new Response(false, "Usage: remove_key <key>");
        }

        int key;
        try{
            key = Integer.parseInt(arg);
        } catch (NumberFormatException ex){
            return new Response(false, "Key must be integer.");
        }

        try {
            boolean isRemoved = routeRepository.removeByKey(key, userid);
            if (isRemoved) {
                collectionManager.removeByKey(key, userid);
                return new Response(true, "Successfully removed!");
            }
            return new Response(false, "Key does not exists");
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "remove_key";
    }

    @Override
    public String getDescription() {
        return "removes element using a key";
    }
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class ReplaceIfLower implements ClientCommand {

    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public ReplaceIfLower(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg == null || arg .isBlank()) {
            return new Response(false, "Usage: replace_if_lower <key>");
        }

        if (route == null){
            return new Response(false, "Route must not be null");
        }

        int key;
        try{
            key = Integer.parseInt(arg);
        }catch (NumberFormatException ex){
           return new Response(false, "Key must be integer.");
        }

        try {
            boolean isReplaced = routeRepository.replaceIfLower(key, route, userId);
            if (isReplaced) {
                collectionManager.replaceIfLower(key, route, userId);
                return  new Response(true, "Element replaced!");
            }
            return  new Response(true, "New element is more than old or they're equals.");
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "replace_if_lower";
    }

    @Override
    public String getDescription() {
        return "replaces element using a key,\nif new value is less than old";
    }
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class Update implements ClientCommand {

    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public Update(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg == null || arg.isBlank()){
            return new Response(false,"Usage: update <key>");
        }

        if (route == null){
            return new Response(false, "Route must not be null.");
        }

        int key;
        try{
            key = Integer.parseInt(arg);
        } catch (NumberFormatException ex){
            return new Response(false, "Key must be integer.");
        }

        try {
            boolean isUpdted = routeRepository.update(key, route, userId);
            if (isUpdted){
                collectionManager.update(key, route, userId);
                return new Response(true, "Element updated!");
            }
            return new Response(false, "Key does not exists");
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public String getDescription() {
        return "updates element using a key";
    }
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;
import java.sql.SQLException;

public class Insert implements ClientCommand {
    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public Insert(CollectionManager collectionManager, RouteRepository routeRepository) {
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {
        if (route == null) {
            return new Response(false, "Route must not be null");
        }

        int key;
        try {
            key = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return new Response(false, "Key must be Integer.");
        }

        if (userId == null) {
            return new Response(false, "You must be logged in to insert");
        }

        try {
            if (routeRepository.insert(key, route, userId)) {
                collectionManager.insert(key, route, userId);
                return new Response(true, "Element inserted");
            } else {
                return new Response(false, "Failed to insert into database");
            }
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "insert";
    }

    @Override
    public String getDescription() {
        return "adds new element using a key";
    }
}package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class RemoveGreaterKey implements ClientCommand {

    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public RemoveGreaterKey(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg == null || arg.isBlank()){
            return new Response(false, "Usage: remove_greater_key <key>");
        }

        int key;
        try{
            key = Integer.parseInt(arg);
        } catch (NumberFormatException ex){
            return new Response(false, "Key must be integer.");
        }

        try {
            int removed = routeRepository.removeGreaterKey(key, userId);
            if (removed > 0){
                collectionManager.removeGreaterKey(key, userId);
                return new Response(true, "Successfully removed!" + "Number of removed elements: " + removed);
            }
            return new Response(true, "Collection size didn't change.");
        } catch (SQLException e) {
            return new Response(false, "Database error: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "remove_greater_key";
    }

    @Override
    public String getDescription() {
        return "removes all collection elements,\nwhich key is more than inserted value";
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public class CountByDistance implements ClientCommand {

    private final CollectionManager collectionManager;

    public CountByDistance(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg == null || arg.isBlank()){
            return new Response(false, "Usage: count_by_distance <distance>");
        }

        double distance;
        long count;

        try{
            distance = Double.parseDouble(arg);
            count =  collectionManager.countByDistance(distance);
        } catch (NumberFormatException ex){
            return new Response(false, "Usage: count_by_distance <distance>");
        }

        return new Response(true, "There are " + count + " objects with same distance");
    }

    @Override
    public String getName() {
        return "count_by_distance";
    }

    @Override
    public String getDescription() {
        return "shows number of elements, which distance fields\nare equals to inserted value";
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public class Info implements ClientCommand {
    private final CollectionManager collectionManager;

    public Info(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String arg, Route route, Long userId)
    {
        if (arg != null){
            return new Response(false, "Usage: info");
        }
        return new Response(true, collectionManager.info()) ;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "shows info about collection";
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.util.List;

public class FilterLessThanDistance implements ClientCommand {

    private final CollectionManager collectionManager;

    public FilterLessThanDistance(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {
        if (arg == null || arg.isBlank()) {
            return new Response(false, "Usage: filter_less_than_distance <distance>");
        }

        List<Route> out;
        double distance;
        try {
            distance = Double.parseDouble(arg);
            out = collectionManager.filterLessThanDistance(distance);
        } catch (NumberFormatException ex) {
            return new Response(false, "Usage: filter_less_than_distance <distance>");
        }

        return new Response(true, "Routes with distance < " + distance + ":", out);
    }

    @Override
    public String getName() {
        return "filter_less_than_distance";
    }

    @Override
    public String getDescription() {
        return "shows elements, which distance field\nis less than inserted value";
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public class CheckInsertKey implements ClientCommand {
    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public CheckInsertKey(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg == null || arg.isBlank()){
            return new Response(false, "Usage: insert <key>");
        }

        int key;
        try {
            key = Integer.parseInt(arg);
        } catch (NumberFormatException ex){
            return new Response(false, "Key must be integer.");
        }

        if (collectionManager.checkKey(key)){
            return new Response(false, "Key already exists.");
        } else {
            return new Response(true, "Key is available!");
        }
    }

    @Override
    public String getName() {
        return "check_key";
    }

    @Override
    public String getDescription() {
        return "checks_key";
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.sql.SQLException;

public class CheckUpdateKey implements ClientCommand {
    private final CollectionManager collectionManager;
    private final RouteRepository routeRepository;

    public CheckUpdateKey(CollectionManager collectionManager, RouteRepository routeRepository){
        this.collectionManager = collectionManager;
        this.routeRepository = routeRepository;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg == null || arg.isBlank()) {
            return new Response(false, "Usage: insert <key>");
        }

        int key;
        try {
            key = Integer.parseInt(arg);
        } catch (NumberFormatException ex) {
            return new Response(false, "Key must be integer.");
        }

        if (!collectionManager.checkKey(key)) {
            return new Response(false, "Key doesn't exist.");
        }

        try {
            if (!routeRepository.checkOwnership(key, userId)){
                return new Response(false, "You can only modify your own routes.");
            }
        } catch (SQLException e) {
            return new Response(false, "DB error: " + e.getMessage());
        }
        return new Response(true, "Key exists and belongs to you. Enter new data:");
    }

    @Override
    public String getName() {
        return "check_key";
    }

    @Override
    public String getDescription() {
        return "checks_key";
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.manager.ServerCommandExecutor;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public class Help implements ClientCommand {

    private final ServerCommandExecutor serverCommandExecutor;

    public Help(ServerCommandExecutor serverCommandExecutor){
        this.serverCommandExecutor = serverCommandExecutor;
    }


    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg != null){
            return new Response(false, "Usage: help");
        }

        String answer = "Available commands:\n\n--execute_script--\nexecutes script\n";
        for (ClientCommand clientCommand : serverCommandExecutor.getCommands().values()){
            if (clientCommand.getName().equals("save") || clientCommand.getName().equals("check_key")){
                continue;
            }
            answer += " \n";
            answer += "--" + clientCommand.getName() + "--\n";
            answer += clientCommand.getDescription()+ "\n";
        }

        return new Response(true, answer);
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "shows available commands";
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public class Show implements ClientCommand {

    private final CollectionManager collectionManager;

    public Show(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {
        if (arg != null) {
            return new Response(false, "Usage: show");
        }
        return new Response(true, "All available routes:", collectionManager.showCollection());
    }

    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getDescription() {
        return "shows all collection elements";
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.ClientCommand;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

public class AverageOfDistance implements ClientCommand {

    private final CollectionManager collectionManager;

    public AverageOfDistance(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String arg, Route route, Long userId) {

        if (arg != null){
            return new Response(false, "Usage: average_of_distance");
        }

        double avgDist = collectionManager.averageOfDistance();

        return new Response(true, "Average distance: " + avgDist);
    }

    public String getName() {
        return "average_of_distance";
    }

    @Override
    public String getDescription() {
        return "gives you average of distance fields in collection";
    }
}
package org.Gh0st1yAnge1.db;

import org.Gh0st1yAnge1.model.Coordinates;
import org.Gh0st1yAnge1.model.Location;
import org.Gh0st1yAnge1.model.Route;

import java.sql.*;
import java.util.LinkedHashMap;

public class RouteRepository {
    private final DatabaseManager db;

    public RouteRepository(DatabaseManager db) {
        this.db = db;
    }

    public LinkedHashMap<Integer, Route> loadCollection() throws SQLException {
        LinkedHashMap<Integer, Route> routes = new LinkedHashMap<>();
        String sql = "SELECT * FROM routes;";

        try (Connection conn = db.getConnection(); Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                int key = rs.getInt("key_id");
                Route route = mapRowToRoute(rs);
                routes.put(key, route);
            }
        }
        return routes;
    }

    public boolean insert(int key, Route route, long ownerId) throws SQLException {

        String sql = """
        INSERT INTO routes (key_id, name, coord_x, coord_y, creation_date,
        from_x, from_y, from_z, from_name, to_x, to_y, to_z, to_name, distance, owner_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, key);
            ps.setString(2, route.getName());
            ps.setDouble(3, route.getCoordinates().getX());
            ps.setFloat(4, route.getCoordinates().getY());
            ps.setTimestamp(5, Timestamp.valueOf(route.getCreationDate().atStartOfDay()));
            setLocation(ps, 6, route.getFrom());
            setLocation(ps, 10, route.getTo());
            ps.setFloat(14, route.getDistance());
            ps.setLong(15, ownerId);

            route.setOwnerId(ownerId);
            route.setKey((long) key);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(int key, Route route, long ownerId) throws SQLException {
        if (!checkOwnership(key, ownerId)) return false;

        String sql = """
        UPDATE routes SET
            name = ?, coord_x = ?, coord_y = ?,
            from_x = ?, from_y = ?, from_z = ?, from_name = ?,
            to_x = ?, to_y = ?, to_z = ?, to_name = ?,
            distance = ?
        WHERE key_id = ? AND owner_id = ?;
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, route.getName());
            ps.setDouble(2, route.getCoordinates().getX());
            ps.setFloat(3, route.getCoordinates().getY());
            setLocation(ps, 4, route.getFrom());
            setLocation(ps, 8, route.getTo());
            ps.setFloat(12, route.getDistance());
            ps.setInt(13, key);
            ps.setLong(14, ownerId);

            route.setKey((long) key);
            route.setOwnerId(ownerId);

            return ps.executeUpdate() > 0;
        }
    }

    private void setLocation(PreparedStatement ps, int idx, Location loc) throws SQLException {
        if (loc == null) {
            ps.setNull(idx,     Types.DOUBLE);
            ps.setNull(idx + 1, Types.DOUBLE);
            ps.setNull(idx + 2, Types.INTEGER);
            ps.setNull(idx + 3, Types.VARCHAR);
        } else {
            ps.setDouble(idx, loc.getX());
            ps.setDouble(idx + 1, loc.getIntY());
            if (loc.getIntZ() == null) {
                ps.setNull(idx + 2, Types.INTEGER);
            } else {
                ps.setInt(idx + 2, loc.getIntZ());
            }
            if (loc.getName() == null) {
                ps.setNull(idx + 3, Types.VARCHAR);
            } else {
                ps.setString(idx + 3, loc.getName());
            }
        }
    }

    public boolean removeByKey(int key, long ownerId) throws SQLException {
        String sql = "DELETE FROM routes WHERE key_id=? AND owner_id=?;";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, key);
            ps.setLong(2, ownerId);
            return ps.executeUpdate() > 0;
        }
    }

    public int removeGreaterKey(int key, long ownerId) throws SQLException {
        String sql = "DELETE FROM routes WHERE key_id>? AND owner_id=?;";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, key);
            ps.setLong(2, ownerId);
            return ps.executeUpdate();
        }
    }

    public int removeGreater(Route route, long ownerId) throws SQLException {
        String sql = "DELETE FROM routes WHERE distance>? AND owner_id=?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, route.getDistance());
            ps.setLong(2, ownerId);
            return ps.executeUpdate();
        }
    }

    public void clear(long ownerId) throws SQLException {
        String sql = "DELETE FROM routes WHERE owner_id=?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ownerId);
            ps.executeUpdate();
        }
    }

    public boolean replaceIfLower(int key, Route route, long ownerId) throws SQLException {
        String sql = "SELECT distance FROM routes WHERE key_id=? AND owner_id=?;";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, key);
            ps.setLong(2, ownerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                float oldDistance = rs.getFloat("distance");
                if (route.getDistance() < oldDistance) {
                    return update(key, route, ownerId);
                }
            }
            return false;
        }
    }

    public boolean checkOwnership(int key, long ownerId) throws SQLException {
        String sql = "SELECT 1 FROM routes WHERE key_id=? and owner_id=?;";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, key);
            ps.setLong(2, ownerId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public Route mapRowToRoute(ResultSet rs) throws SQLException {
        Route route = new Route();
        route.setKey(rs.getLong("key_id"));
        route.setOwnerId(rs.getLong("owner_id"));
        route.setName(rs.getString("name"));

        // Coordinates
        Coordinates coords = new Coordinates(
                rs.getFloat("coord_x"),
                rs.getInt("coord_y")
        );
        route.setCoordinates(coords);

        // Dates
        Timestamp ts = rs.getTimestamp("creation_date");
        if (ts != null) route.setCreationDate(ts.toLocalDateTime().toLocalDate());

        route.setFrom(readLocation(rs, "from_x", "from_y", "from_z", "from_name"));
        route.setTo(readLocation(rs, "to_x", "to_y", "to_z", "to_name"));

        route.setDistance(rs.getLong("distance"));

        return route;
    }

    private Location readLocation(ResultSet rs, String xCol, String yCol, String zCol, String nameCol) throws SQLException {
        double x = rs.getDouble(xCol);
        if (rs.wasNull()) {
            return null;
        }
        double y = rs.getDouble(yCol);
        int zRaw = rs.getInt(zCol);
        Integer z = rs.wasNull() ? null : zRaw;
        String name = rs.getString(nameCol);
        return new Location(x, y, z, name);
    }
}
package org.Gh0st1yAnge1.db;


import org.Gh0st1yAnge1.auth.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    private final DatabaseManager db;
    private final PasswordHasher hasher;

    public UserRepository(DatabaseManager db, PasswordHasher hasher){
        this.db = db;
        this.hasher = hasher;
    }

    public boolean register(String username, String password) throws SQLException {
        String hashedPassword = hasher.hash(password);
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.executeUpdate();
            return true;
        } catch (SQLException e){
            if (e.getSQLState().equals("23505")) return false;
            throw e;
        }
    }

    public Long authenticate(String username, String password) throws SQLException {
        String sql = "SELECT id, password_hash FROM users WHERE username = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                long id = rs.getLong("id");
                String storedHash = rs.getString("password_hash");
                if (hasher.verify(password, storedHash)){
                    return id;
                }
            }
            return null;
        }
    }
}package org.Gh0st1yAnge1.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager(String url, String user, String password) throws SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
        initTables();
    }

    private void initTables() throws SQLException {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(64) UNIQUE NOT NULL,
                password_hash VARCHAR(512) NOT NULL
            );
        """;

        String createRoutes = """
            CREATE TABLE IF NOT EXISTS routes (
                key_id INTEGER PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                coord_x DOUBLE PRECISION,
                coord_y INTEGER,
                creation_date TIMESTAMP,
                from_x DOUBLE PRECISION,
                from_y DOUBLE PRECISION,
                from_z INTEGER,
                from_name TEXT,
                to_x FLOAT,
                to_y DOUBLE PRECISION,
                to_z INTEGER,
                to_name TEXT,
                distance BIGINT,
                owner_id INTEGER REFERENCES users(id) ON DELETE CASCADE
            );
        """;

        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.execute(createUsers);
            st.execute(createRoutes);
            System.out.println("Users and Routes tables are created/checked");
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void close() {
        System.out.println("DatabaseManager closed (DriverManager)");
    }
}package org.Gh0st1yAnge1.audit;

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
}package org.Gh0st1yAnge1;

import org.Gh0st1yAnge1.audit.AuditProducer;
import org.Gh0st1yAnge1.auth.AuthService;
import org.Gh0st1yAnge1.auth.HasherFactory;
import org.Gh0st1yAnge1.auth.PasswordHasher;
import org.Gh0st1yAnge1.config.Config;
import org.Gh0st1yAnge1.db.DatabaseManager;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.db.UserRepository;
import org.Gh0st1yAnge1.manager.CollectionManager;
import org.Gh0st1yAnge1.manager.InputManager;
import org.Gh0st1yAnge1.manager.ServerCommandExecutor;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Request;
import org.Gh0st1yAnge1.request_and_response.Response;
import org.Gh0st1yAnge1.utils.LoggerUtil;
import org.Gh0st1yAnge1.utils.TcpUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerApp {

    private static final int PORT = 12345;
    private static final Logger logger = LoggerUtil.getLogger();

    private static UserRepository userRepository;
    private static AuthService authService;
    private static PasswordHasher passwordHasher;
    private static Config config;
    private static DatabaseManager databaseManager;
    private static CollectionManager collectionManager;
    private static ServerCommandExecutor serverCommandExecutor;
    private static InputManager inputManager;
    private static AuditProducer auditProducer;
    private static volatile boolean running = true;

    private static final ExecutorService readPool =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private static final ForkJoinPool processPool = new ForkJoinPool();
    private static final ForkJoinPool sendPool = new ForkJoinPool();

    private static final Set<SelectionKey> closingKeys = ConcurrentHashMap.newKeySet();
    private static final ConcurrentLinkedQueue<Runnable> selectorTasks = new ConcurrentLinkedQueue<>();
    private static volatile Selector mainSelector;

    public static void main(String[] args) throws SQLException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%6$s%n");
        logger.setLevel(Level.INFO);
        logger.info("=== LOADING SERVER ===");

        try {
            config = new Config();
            config.printConfig();
            databaseManager = new DatabaseManager(
                    config.getDbUrl(),
                    config.getDbUser(),
                    config.getDbPassword()
            );
            System.out.println("DB started!");
        } catch (Exception e) {
            logger.severe("DB isn't connected: " + e.getMessage() + ". Shutting down.");
            return;
        }

        passwordHasher = HasherFactory.create(config.getHashAlgorithm());
        logger.info("Using hash algorithm " + config.getHashAlgorithm());
        userRepository = new UserRepository(databaseManager, passwordHasher);
        authService = new AuthService(userRepository);

        RouteRepository routeRepository = new RouteRepository(databaseManager);

        LinkedHashMap<Integer, Route> loadedData = routeRepository.loadCollection();
        collectionManager = new CollectionManager();
        collectionManager.load(loadedData);
        logger.info("Collection loaded. Elements count: " + loadedData.size());

        String kafkaBootstrap = config.getKafkaBootstrapServers();
        if (kafkaBootstrap != null && !kafkaBootstrap.trim().isEmpty()) {
            try {
                auditProducer = new AuditProducer(kafkaBootstrap);
                logger.info("Kafka audit enabled. Bootstrap: " + kafkaBootstrap);
            } catch (Exception e) {
                logger.warning("Failed to init Kafka: " + e.getMessage() + ". Running without audit.");
                auditProducer = null;
            }
        } else {
            logger.info("KAFKA_BOOTSTRAP_SERVERS not set. Running without audit.");
        }

        serverCommandExecutor = new ServerCommandExecutor(collectionManager, auditProducer, authService, routeRepository);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (auditProducer != null) {
                auditProducer.close();
                logger.info("AuditProducer closed.");
            }
        }));

        logger.info("Starting server loop...");
        runServer();
    }

    private static void runServer() {
        Thread networkThread = new Thread(() -> {
            try (Selector selector = Selector.open();
                 ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

                serverChannel.configureBlocking(false);
                serverChannel.bind(new InetSocketAddress(PORT));
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                mainSelector = selector;
                logger.info("Server started on port: " + PORT);

                while (running) {
                    selector.select(200);
                    Runnable task;
                    while ((task = selectorTasks.poll()) != null) task.run();

                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (!key.isValid()) continue;

                        try {
                            if (key.isAcceptable()) {
                                handleAccept(key, selector);
                            }
                            if (key.isValid() && key.isReadable()) {
                                key.interestOps(0);
                                readPool.submit(() -> dispatchRead(key));
                            }
                        } catch (IOException e) {
                            logger.severe("Accept error: " + e.getMessage());
                            closeClient(key);
                        }
                    }
                }
            } catch (IOException e) {
                logger.severe("Server startup error: " + e.getMessage());
            }
        });

        networkThread.setDaemon(true);
        networkThread.start();

        inputManager = new InputManager();
        while (running) {
            System.out.print("> ");
            String input = inputManager.readline();
            if (input == null || input.trim().isEmpty()) continue;
            String answer = serverCommandExecutor.execute(input);
            if (answer == null) continue;
            if (answer.equals("Session terminated.")) {
                if (auditProducer != null) {
                    auditProducer.close();
                    logger.info("AuditProducer closed.");
                }
                System.out.println(answer);
                running = false;
                System.exit(0);
            }
            System.out.println(answer);
        }
    }

    private static void handleAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) return;
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ, new ClientSession());
        logger.info("New connection from: " + clientChannel.getRemoteAddress());
    }

    private static void dispatchRead(SelectionKey key) {
        try {
            boolean dispatched = handleRead(key);
            if (!dispatched) {
                rearmRead(key);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (e.getMessage() != null) logger.warning("Read error: " + e.getMessage());
            closeClient(key);
        }
    }

    private static void rearmRead(SelectionKey key) {
        selectorTasks.add(() -> {
            if (key.isValid()) key.interestOps(SelectionKey.OP_READ);
        });
        if (mainSelector != null) mainSelector.wakeup();
    }

    private static boolean handleRead(SelectionKey key) throws IOException, ClassNotFoundException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ClientSession session = (ClientSession) key.attachment();

        while (true) {
            if (session.remainingChunkBytes == null) {
                int bytesRead = clientChannel.read(session.chunkLengthBuffer);
                if (bytesRead == -1) { closeClient(key); return true; }
                if (session.chunkLengthBuffer.hasRemaining()) return false;

                session.chunkLengthBuffer.flip();
                int chunkLength = session.chunkLengthBuffer.getInt();
                session.chunkLengthBuffer.clear();

                if (chunkLength < 0) { closeClient(key); return true; }

                if (chunkLength == 0) {
                    session.payloadOutputStream.flush();
                    Request request = (Request) TcpUtil.desirealizeFromFile(session.payloadFile);
                    logger.info("Request: " + request.commandType()
                            + " from " + clientChannel.getRemoteAddress());
                    processPool.submit(() -> {
                        try {
                            Response response = serverCommandExecutor.execute(request);
                            sendPool.submit(() -> {
                                try {
                                    sendResponse(clientChannel, response);
                                    session.reset();
                                    rearmRead(key);
                                } catch (IOException e) {
                                    logger.warning("Send error: " + e.getMessage());
                                    closeClient(key);
                                }
                            });
                        } catch (Exception e) {
                            logger.warning("Process error: " + e.getMessage());
                            closeClient(key);
                        }
                    });
                    return true;
                }

                session.remainingChunkBytes = chunkLength;
            }

            session.payloadBuffer.clear();
            int portion = Math.min(session.remainingChunkBytes, session.payloadBuffer.capacity());
            session.payloadBuffer.limit(portion);
            int bytesRead = clientChannel.read(session.payloadBuffer);
            if (bytesRead == -1) { closeClient(key); return true; }
            if (bytesRead == 0) return false;
            session.payloadOutputStream.write(session.payloadBuffer.array(), 0, bytesRead);
            session.remainingChunkBytes -= bytesRead;
            if (session.remainingChunkBytes == 0) session.remainingChunkBytes = null;
        }
    }

    private static void sendResponse(SocketChannel channel, Response response) throws IOException {
        Path payloadFile = TcpUtil.serializeToTempFile(response);
        try {
            TcpUtil.writeChunkedFromFile(channel, payloadFile);
        } finally {
            Files.deleteIfExists(payloadFile);
        }
    }

    private static void closeClient(SelectionKey key) {
        if (!closingKeys.add(key)) return;
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            try { logger.info("Client disconnected: " + channel.getRemoteAddress()); }
            catch (IOException ignored) {}
            ClientSession session = (ClientSession) key.attachment();
            if (session != null) { try { session.cleanUp(); } catch (IOException ignored) {} }
            key.cancel();
            try { channel.close(); } catch (IOException ignored) {}
        } finally {
            closingKeys.remove(key);
        }
    }

    private static class ClientSession {
        ByteBuffer chunkLengthBuffer = ByteBuffer.allocate(4);
        ByteBuffer payloadBuffer     = ByteBuffer.allocate(TcpUtil.CHUNK_SIZE);
        Integer remainingChunkBytes  = null;
        Path payloadFile;
        OutputStream payloadOutputStream;

        ClientSession() throws IOException { init(); }

        void init() throws IOException {
            chunkLengthBuffer.clear();
            remainingChunkBytes = null;
            payloadBuffer.clear();
            payloadFile = Files.createTempFile("tcp-request", ".bin");
            payloadOutputStream = Files.newOutputStream(payloadFile);
        }

        void cleanUp() throws IOException {
            if (payloadOutputStream != null) payloadOutputStream.close();
            if (payloadFile != null) Files.deleteIfExists(payloadFile);
        }

        void reset() throws IOException { cleanUp(); init(); }
    }
}
package org.Gh0st1yAnge1.manager;

import org.Gh0st1yAnge1.model.Route;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CollectionManager {

    private final ConcurrentHashMap<Integer, Route> collection = new ConcurrentHashMap<>();
    private final LocalDate initializationDate = LocalDate.now();

    public void load(Map<Integer, Route> loaded) {
        collection.clear();
        if (loaded != null) collection.putAll(loaded);
    }

    public boolean checkKey(int key) {
        return collection.containsKey(key);
    }

    public void insert(Integer key, Route route, long userId) {
        route.setOwnerId(userId);
        route.setKey((long) key);
        collection.put(key, route);
    }

    public boolean update(Integer key, Route route, long userId) {
        Route existing = collection.get(key);
        if (existing != null && existing.getOwnerId() == userId) {
            route.setOwnerId(userId);
            route.setKey((long) key);
            collection.put(key, route);
            return true;
        }
        return false;
    }

    public boolean removeByKey(Integer key, long userId) {
        Route route = collection.get(key);
        if (route != null && route.getOwnerId() == userId) {
            return collection.remove(key) != null;
        }
        return false;
    }

    public int removeGreaterKey(Integer key, long userId) {
        List<Integer> toRemove = collection.entrySet().stream()
                .filter(entry -> entry.getValue().getOwnerId() == userId)
                .filter(entry -> entry.getKey() > key)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        toRemove.forEach(collection::remove);
        return toRemove.size();
    }

    public int removeGreater(Route ref, long userId) {
        List<Integer> toRemove = collection.entrySet().stream()
                .filter(entry -> entry.getValue().getOwnerId() == userId)
                .filter(entry -> entry.getValue().compareTo(ref) > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        toRemove.forEach(collection::remove);
        return toRemove.size();
    }

    public boolean replaceIfLower(Integer key, Route newRoute, long userId) {
        boolean[] replaced = {false};
        collection.compute(key, (k, current) -> {
            if (current != null && current.getOwnerId() == userId && current.compareTo(newRoute) > 0) {
                replaced[0] = true;
                newRoute.setOwnerId(userId);
                newRoute.setKey((long) key);
                return newRoute;
            }
            return current;
        });
        return replaced[0];
    }


    public void clearOnly(long userId) {
        collection.entrySet().removeIf(e -> e.getValue().getOwnerId() == userId);
    }

    public Map<Integer, Route> getMap() {
        return Collections.unmodifiableMap(collection);
    }

    public List<Route> showCollection() {
        return collection.values().stream()
                .sorted(Comparator.comparing(Route::getTo,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public double averageOfDistance() {
        return collection.values().stream()
                .mapToDouble(Route::getDistance)
                .average()
                .orElse(0.0);
    }

    public long countByDistance(double distance) {
        return collection.values().stream()
                .filter(r -> r.getDistance() == distance)
                .count();
    }

    public List<Route> filterLessThanDistance(double distance) {
        return collection.values().stream()
                .filter(r -> r.getDistance() < distance)
                .collect(Collectors.toList());
    }

    public String info() {
        return "Collection type: ConcurrentHashMap (java.util.concurrent)" +
                "\nSize: " + collection.size() +
                "\nInitialization date: " + initializationDate;
    }
}package org.Gh0st1yAnge1.manager;

import org.Gh0st1yAnge1.exceptions.InputCancelledException;
import org.Gh0st1yAnge1.model.Coordinates;
import org.Gh0st1yAnge1.model.Location;
import org.Gh0st1yAnge1.utils.Validator;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

public class InputManager {

    private final Deque<Scanner> scannerStack = new ArrayDeque<>();

    public InputManager(){
        scannerStack.push(new Scanner(System.in));
    }

    private Scanner currentScanner(){
        return scannerStack.peek();
    }

    public String readline(){

        if (isScriptMode()){
            Scanner scanner = currentScanner();
            if (!scanner.hasNextLine()){
                return null;
            }
            return scanner.nextLine().trim();
        }

        StringBuilder buffer = new StringBuilder();

        try{
            while (true){
                int ch = System.in.read();

                if (ch == -1){
                    return null;
                }

                if (ch == 27) {           //ESC
                    System.in.read();     //[
                    System.in.read();     //A/B/C/D
                    continue;
                }

                //Enter
                if (ch == '\n' || ch == '\r'){
                    System.out.println();
                    return buffer.toString().trim();
                }

                //Ctrl+C
                if (ch == 3){
                    System.out.println("Watafa");
                    return null;
                }

                //Ctrl+D
                if (ch == 4){
                    System.out.println("Yooooo, buddy, what's going on?\nU wanted to use 'Ctrl+D'?\nOh nooo, it doesn't works(\nChill out baby)");
                    return null;
                }

                //Ctrl+Z
                if (ch == 26){
                    System.out.println("Pepe shneine");
                    return null;
                }

                //Backspace
                if (ch == 127 || ch == 8){
                    if (buffer.length() > 0){
                        buffer.deleteCharAt(buffer.length()-1);
                        System.out.print("\b \b");
                    }
                    continue;
                }

                buffer.append((char)ch);
            }
        } catch (IOException e){
            return null;
        }
    }

    public void pushScanner(Scanner scanner){
        scannerStack.push(scanner);
    }

    public void popScanner(){
        if (scannerStack.size() > 1){
            scannerStack.pop();
        }
    }

    public boolean isScriptMode(){
        return scannerStack.size() > 1;
    }

    public <T> T readValue(
            String prompt,
            Function<String, T> parser,
            Predicate<T> validator,
            String errormessage
    ){
        while (true){

            if (!isScriptMode()){
                System.out.print(prompt);
            }

            String input = readline();

            if (input == null){
                throw new InputCancelledException("");
            }

            if(errormessage.contains("and have less than 5 digits after the dot")){
                if (input.contains(",")){
                    input = input.replace(',','.');
                }

                if (!input.matches("\\d+(\\.\\d{1,5})?")){
                    System.out.println(errormessage);
                    continue;
                }
            }

            try{
                T value = parser.apply(input);
                if (validator == null || validator.test(value)){
                    return value;
                }
            } catch (Exception ignored){}

            if (isScriptMode()){
                throw new RuntimeException("Invalid value in script.");
            }

            System.out.println(errormessage);
        }
    }
    //Location +

    public String readLocationName(){
        if (!isScriptMode()){
            System.out.println("Press 'Enter' to insert null or type location name: ");
        }

        String input = readline();

        if (input == null){
            throw new InputCancelledException("");
        }

        if (input.isEmpty()){
            return null;
        }

        return input;
    }

    public Double readLocationDoubleX(){
        return readValue(
                "Enter the 'double' type coordinate X: ",
                Double::parseDouble,
                null,
                "Coordinate X must have type 'double' and have less than 5 digits after the dot."
        );
    }

    public Double readLocationDoubleY(){
        return readValue(
                "Enter the 'float' type coordinate Y: ",
                Double::parseDouble,
                null,
                "Coordinate Y must have type 'float' and have less than 5 digits after the dot!"
        );
    }

    public Integer readLocationIntegerZ(){
        return readValue(
                "Enter the 'Integer' type coordinate Z: ",
                Integer::parseInt,
                Validator::validateLocationZ,
                "Coordinate Z must have type 'Integer'."
        );
    }


    //Coordinates +

    public Float readCoordinateFloatX(){
        return readValue(
                "Enter the 'Float' type coordinate X: ",
                Float::parseFloat,
                Validator::validateCoordinateX,
                "Coordinate X must have type 'Float' and have less than 5 digits after the dot."
        );
    }

    public Float readCoordinateFloatY(){
        return readValue(
                "Enter the 'Float' type coordinate Y: ",
                Float::parseFloat,
                Validator::validateCoordinateY,
                "Coordinate Y must have type 'float' and have less than 5 digits after the dot."
        );
    }

    //Route +

    public String readRouteName(){
        return readValue(
                "Enter the Route name: ",
                Function.identity(),
                Validator::validateRouteName,
                "Route name must not be empty!"
        );
    }

    public Float readRouteDistance(){
        return readValue(
                "Enter the 'float' type Route distance: ",
                Float::parseFloat,
                Validator::validateRouteDistance,
                "Route distance must have type 'float'."
        );
    }

    public Coordinates readCoordinates(){
        return new Coordinates(readCoordinateFloatX(), readCoordinateFloatY());
    }

    public Location readLocation(){

        Location location = null;
        if (!isScriptMode()){
            System.out.println("Press 'Enter' to insert null or type 'location' to create Location");
        }

        while(true){
            String line = readline();

            if (line == null){
                throw new InputCancelledException("");
            }
            if (line.trim().isEmpty()){
                return location;
            } else if (line.trim().equals("location")) {
                break;
            } else {
                System.out.println("Press 'Enter' or type 'location'");
            }
        }

        System.out.println("Creating Location...");

        int type = readValue(
                "Choose type '1' or '2':\n1 - (x, y, z)\n2 - (x, y, z, name)\n",
                Integer::parseInt,
                Validator::validateLocationType,
                "Type '1' or '2'"
        );

        switch (type){
            case 1 -> location = new Location(readLocationDoubleX(), readLocationDoubleY(), readLocationIntegerZ());
            case 2 -> location = new Location(readLocationDoubleX(), readLocationDoubleY(), readLocationIntegerZ(), readLocationName());
        }
        return location;
    }
}package org.Gh0st1yAnge1.manager;

import org.Gh0st1yAnge1.audit.AuditProducer;
import org.Gh0st1yAnge1.auth.AuthService;
import org.Gh0st1yAnge1.client_commands.*;
import org.Gh0st1yAnge1.client_commands.auth.Login;
import org.Gh0st1yAnge1.client_commands.auth.Logout;
import org.Gh0st1yAnge1.client_commands.auth.Register;
import org.Gh0st1yAnge1.client_commands.read.*;
import org.Gh0st1yAnge1.client_commands.write.*;
import org.Gh0st1yAnge1.db.RouteRepository;
import org.Gh0st1yAnge1.exceptions.InputCancelledException;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;
import org.Gh0st1yAnge1.request_and_response.Response;
import org.Gh0st1yAnge1.server_commands.Exit;
import org.Gh0st1yAnge1.server_commands.SaveWithPath;
import org.Gh0st1yAnge1.server_commands.ServerCommand;
import org.Gh0st1yAnge1.server_commands.ServerHelp;

import java.sql.SQLException;
import java.util.*;

public class ServerCommandExecutor {

    private final Map<String, ClientCommand> clientCommands = new LinkedHashMap<>();
    private final Map<String, ServerCommand> serverCommands = new LinkedHashMap<>();
    private final AuditProducer auditProducer;
    private final AuthService authService;
    private final RouteRepository routeRepository;

    private static final Set<CommandType> AUDITED_COMMANDS = Set.of(
            CommandType.INSERT,
            CommandType.UPDATE,
            CommandType.REMOVE_KEY,
            CommandType.REMOVE_GREATER_KEY,
            CommandType.REMOVE_GREATER,
            CommandType.REPLACE_IF_LOWER,
            CommandType.CLEAR,
            CommandType.SHOW
    );

    private static final Set<String> NO_AUTH = Set.of("register", "login", "logout", "help");

    public ServerCommandExecutor(CollectionManager collectionManager,
                                 AuditProducer auditProducer,
                                 AuthService authService,
                                 RouteRepository routeRepository) {
        this.auditProducer = auditProducer;
        this.authService = authService;
        this.routeRepository = routeRepository;

        // read
        clientCommands.put("average_of_distance", new AverageOfDistance(collectionManager));
        clientCommands.put("help", new Help(this));
        clientCommands.put("info", new Info(collectionManager));
        clientCommands.put("show", new Show(collectionManager));
        clientCommands.put("count_by_distance", new CountByDistance(collectionManager));
        clientCommands.put("filter_less_than_distance", new FilterLessThanDistance(collectionManager));
        clientCommands.put("check_insert_key", new CheckInsertKey(collectionManager, routeRepository));
        clientCommands.put("check_update_key", new CheckUpdateKey(collectionManager, routeRepository));

        // write
        clientCommands.put("clear", new Clear(collectionManager, routeRepository));
        clientCommands.put("remove_key", new RemoveKey(collectionManager, routeRepository));
        clientCommands.put("remove_greater_key", new RemoveGreaterKey(collectionManager, routeRepository));
        clientCommands.put("insert", new Insert(collectionManager, routeRepository));
        clientCommands.put("update", new Update(collectionManager, routeRepository));
        clientCommands.put("replace_if_lower", new ReplaceIfLower(collectionManager, routeRepository));
        clientCommands.put("remove_greater", new RemoveGreater(collectionManager, routeRepository));

        // auth
        clientCommands.put("login", new Login(authService));
        clientCommands.put("logout", new Logout());
        clientCommands.put("register", new Register(authService));

        serverCommands.put("exit", new Exit());
        serverCommands.put("save_with_path", new SaveWithPath(collectionManager));
        serverCommands.put("help_server", new ServerHelp(this));
    }

    public Response execute(Request request) throws SQLException {
        String name = request.commandType().toString().toLowerCase();
        ClientCommand command = clientCommands.get(name);
        if (command == null) {
            return new Response(false, "Unknown command. Type 'help' to see available commands");
        }

        Long userId = null;
        if (!NO_AUTH.contains(name)) {
            if (request.login() == null || request.password() == null) {
                return new Response(false, "You must login first! Use: login <login> <password>");
            }
            try {
                userId = authService.login(request.login(), request.password());
                if (userId == null) {
                    return new Response(false, "Authorization failed. Wrong login/password.");
                }
            } catch (SQLException e) {
                return new Response(false, "Database connection error! Server cannot verify your credentials right now.");
            }
        }

        String effectiveArg = request.argument();
        if ((name.equals("register") || name.equals("login")) && request.login() != null) {
            effectiveArg = request.login()
                    + (request.password() != null ? " " + request.password() : "");
        }

        Response response = command.execute(effectiveArg, request.route(), userId);

        if (auditProducer != null && AUDITED_COMMANDS.contains(request.commandType())) {
            auditProducer.sendIfAuditable(
                    request.commandType().name(),
                    request.argument(),
                    response.success(),
                    response.message()
            );
        }

        return response;
    }

    public String execute(String input) {
        if (input == null || input.trim().isEmpty()) return null;

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0];
        String arg = parts.length > 1 ? parts[1] : null;

        ServerCommand command = serverCommands.get(commandName);
        if (command == null) {
            System.out.println("Unknown command. Type 'help_server' to see available commands.");
            return null;
        }

        try {
            return command.execute(arg);
        } catch (InputCancelledException ex) {
            System.out.println("Command cancelled.");
        } catch (Exception e) {
            System.out.println("Error while executing command: " + e.getMessage());
        }
        return null;
    }

    public Map<String, ClientCommand> getCommands() {
        return clientCommands;
    }

    public Map<String, ServerCommand> getServerCommands() {
        return serverCommands;
    }
}
package org.Gh0st1yAnge1.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.utils.IdGenerator;
import org.Gh0st1yAnge1.utils.LocalDateAdapter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;


public class FileManager {
    private final String filePath;

    public FileManager(String filePath){
        this.filePath = filePath;
    }

    public LinkedHashMap<Integer, Route> loadCollection(){

        LinkedHashMap<Integer, Route> map = new LinkedHashMap<>();

        if (filePath == null){
            System.out.println("File path is not found.");
            return map;
        }

        File file = new File(filePath);
        if (!file.exists()){
            System.out.println("File doesn't exists.");
            return map;
        }

        if (!file.canRead()){
            System.out.println("You don't have permission to read this file.");
            return map;
        }

        try (FileReader reader = new FileReader(file)){

            Gson gson =new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).setPrettyPrinting().create();
            Type type = new TypeToken<LinkedHashMap<Integer, Route>>(){}.getType();
            map = gson.fromJson(reader, type);
            if (map == null){
                map = new LinkedHashMap<>();
            }

        } catch (IOException e) {
            System.out.println("Error reading file.");
        }
        return map;
    }

    public String saveCollection(LinkedHashMap<Integer, Route> collection){
        if (filePath == null){
            return ("File path is not found.");
        }

        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()){
            if (!parentDir.mkdirs()){
                return "Failed to create directories: " + filePath;
            }
        }

        try(BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(filePath))){

            Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).setPrettyPrinting().create();
            String json = gson.toJson(collection);

            bos.write(json.getBytes(StandardCharsets.UTF_8));
            bos.flush();

        } catch (IOException e){
            return "Error writing file.";
        }

        return "Collection successfully saved!";
    }

}package org.Gh0st1yAnge1;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
package org.Gh0st1yAnge1.utils;

import org.Gh0st1yAnge1.manager.InputManager;
import org.Gh0st1yAnge1.model.Coordinates;
import org.Gh0st1yAnge1.model.Location;
import org.Gh0st1yAnge1.model.Route;

public class RouteBuilder {

    private final InputManager inputManager;

    public RouteBuilder(InputManager inputManager){
        this.inputManager = inputManager;
    }

    public Route buildRoute(){

        String name = inputManager.readRouteName();
        if (name == null){
            System.out.println("User stopped creating route.");
            return null;
        }
        Coordinates coordinates = inputManager.readCoordinates();
        if (coordinates == null){
            System.out.println("User stopped creating route.");
            return null;
        }
        Location from = inputManager.readLocation();
        Location to = inputManager.readLocation();
        Float distance = inputManager.readRouteDistance();
        if (distance == null){
            System.out.println("user stopped creating route.");
            return null;
        }
        return new Route(name, coordinates, from, to, distance);
    }
}
package org.Gh0st1yAnge1.client_commands.auth;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class Register implements Command {
    @Override
    public Request execute(String args) {
        if (args == null || args.trim().isEmpty()) {
            System.out.println("Usage: register <login> <password>");
            return null;
        }

        String[] parts = args.trim().split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: register <login> <password>");
            return null;
        }
        return new Request(CommandType.REGISTER, null,null, parts[0], parts[1]);
    }
}
package org.Gh0st1yAnge1.client_commands.auth;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class Logout implements Command {
    @Override
    public Request execute(String args) {
        return new Request(CommandType.LOGOUT, null, null, null, null);
    }
}
package org.Gh0st1yAnge1.client_commands.auth;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class Login implements Command {
    @Override
    public Request execute(String args) {
        if (args == null || args.trim().isEmpty()) {
            System.out.println("Usage: login <login> <password>");
            return null;
        }

        String[] parts = args.trim().split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: login <login> <password>");
            return null;
        }
        return new Request(CommandType.LOGIN, null, null, parts[0], parts[1]);
    }
}
package org.Gh0st1yAnge1.client_commands;

import org.Gh0st1yAnge1.request_and_response.Request;

public interface Command {
    Request execute(String args);
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class Clear implements Command {

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.CLEAR, arg, null);
    }
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;
import org.Gh0st1yAnge1.utils.RouteBuilder;

public class RemoveGreater implements Command {

    private final RouteBuilder routeBuilder;

    public RemoveGreater(RouteBuilder routeBuilder){
        this.routeBuilder = routeBuilder;
    }

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.REMOVE_GREATER, arg, routeBuilder.buildRoute());
    }
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class RemoveKey implements Command {

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.REMOVE_KEY, arg, null);
    }
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;
import org.Gh0st1yAnge1.utils.RouteBuilder;

public class ReplaceIfLower implements Command {

    private final RouteBuilder routeBuilder;

    public ReplaceIfLower(RouteBuilder routeBuilder){
        this.routeBuilder = routeBuilder;
    }

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.REPLACE_IF_LOWER, arg, routeBuilder.buildRoute());
    }
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;
import org.Gh0st1yAnge1.utils.RouteBuilder;

public class Update implements Command {

    private final RouteBuilder routeBuilder;

    public Update(RouteBuilder routeBuilder){
        this.routeBuilder = routeBuilder;
    }

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.UPDATE, arg, routeBuilder.buildRoute());
    }
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;
import org.Gh0st1yAnge1.utils.RouteBuilder;

public class Insert implements Command {

    private final RouteBuilder routeBuilder;

    public Insert(RouteBuilder routeBuilder){
        this.routeBuilder = routeBuilder;
    }

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.INSERT, arg, routeBuilder.buildRoute());
    }
}
package org.Gh0st1yAnge1.client_commands.write;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class RemoveGreaterKey implements Command {

    @Override
    public Request execute(String args) {
        return new Request(CommandType.REMOVE_GREATER_KEY, args, null);
    }
}
package org.Gh0st1yAnge1.client_commands;

import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class Exit implements Command {

    @Override
    public Request execute(String args) {
        return new Request(CommandType.EXIT, args, null);
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class CountByDistance implements Command {

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.COUNT_BY_DISTANCE, arg, null);
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class Info implements Command {

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.INFO, arg, null);
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class FilterLessThanDistance implements Command {

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.FILTER_LESS_THAN_DISTANCE, arg, null);
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class CheckInsertKey implements Command {

    @Override
    public Request execute(String args) {
        return new Request(CommandType.CHECK_INSERT_KEY, args, null);
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class CheckUpdateKey implements Command {

    @Override
    public Request execute(String args) {
        return new Request(CommandType.CHECK_UPDATE_KEY, args, null);
    }
}
package org.Gh0st1yAnge1.client_commands.read;


import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class Help implements Command {

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.HELP, arg, null);
    }
}
package org.Gh0st1yAnge1.client_commands.read;


import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class Show implements Command {

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.SHOW, arg, null);
    }
}
package org.Gh0st1yAnge1.client_commands.read;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class AverageOfDistance implements Command {

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.AVERAGE_OF_DISTANCE, arg, null);
    }
}
package org.Gh0st1yAnge1.client_commands;


import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;

public class ExecuteScript implements Command{

    @Override
    public Request execute(String arg) {
        return new Request(CommandType.EXECUTE_SCRIPT, arg, null);
    }
}
package org.Gh0st1yAnge1.gui.models;

import javafx.beans.property.*;
import org.Gh0st1yAnge1.model.Route;

/**
 * JavaFX-обёртка над Route для TableView с Property-полями.
 */
public class RouteTableItem {

    private final IntegerProperty  key        = new SimpleIntegerProperty();
    private final StringProperty   name       = new SimpleStringProperty();
    private final FloatProperty    coordX     = new SimpleFloatProperty();
    private final FloatProperty    coordY     = new SimpleFloatProperty();
    private final StringProperty   creationDate = new SimpleStringProperty();
    private final StringProperty   from       = new SimpleStringProperty();
    private final StringProperty   to         = new SimpleStringProperty();
    private final FloatProperty    distance   = new SimpleFloatProperty();
    private final LongProperty     ownerId    = new SimpleLongProperty();

    private final Route route; // оригинальный объект

    public RouteTableItem(int key, Route route) {
        this.route = route;
        this.key.set(key);
        this.name.set(route.getName());
        if (route.getCoordinates() != null) {
            this.coordX.set(route.getCoordinates().getX());
            this.coordY.set(route.getCoordinates().getY());
        }
        this.creationDate.set(route.getCreationDate() != null
                ? route.getCreationDate().toString() : "");
        this.from.set(route.getFrom() != null ? route.getFrom().toString() : "—");
        this.to.set(route.getTo()     != null ? route.getTo().toString()   : "—");
        this.distance.set(route.getDistance());
    }

    // ── getters for properties ────────────────────────────────────────────────

    public IntegerProperty keyProperty()          { return key; }
    public StringProperty  nameProperty()         { return name; }
    public FloatProperty   coordXProperty()       { return coordX; }
    public FloatProperty   coordYProperty()       { return coordY; }
    public StringProperty  creationDateProperty() { return creationDate; }
    public StringProperty  fromProperty()         { return from; }
    public StringProperty  toProperty()           { return to; }
    public FloatProperty   distanceProperty()     { return distance; }
    public LongProperty    ownerIdProperty()      { return ownerId; }

    public int    getKey()      { return key.get(); }
    public String getName()     { return name.get(); }
    public float  getCoordX()   { return coordX.get(); }
    public float  getCoordY()   { return coordY.get(); }
    public float  getDistance() { return distance.get(); }
    public long   getOwnerId()  { return ownerId.get(); }
    public Route  getRoute()    { return route; }
}package org.Gh0st1yAnge1.gui.network;

import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;
import org.Gh0st1yAnge1.request_and_response.Response;
import org.Gh0st1yAnge1.utils.TcpUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Неблокирующий сетевой клиент на SocketChannel (требование лабы №6).
 * Все методы синхронные — вызывать из фонового потока, не из JavaFX Application Thread.
 */
public class ServerConnection implements AutoCloseable {

    private static final String HOST = "localhost";
    private static final int    PORT = 12345;

    private SocketChannel channel;

    private String login;
    private String password;

    // ── подключение ──────────────────────────────────────────────────────────

    public void connect() throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(HOST, PORT));
        long deadline = System.currentTimeMillis() + 5000;
        while (!channel.finishConnect()) {
            if (System.currentTimeMillis() > deadline)
                throw new IOException("Connection timeout");
            try { Thread.sleep(10); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted");
            }
        }
    }

    public boolean isConnected() {
        return channel != null && channel.isOpen() && channel.isConnected();
    }

    @Override
    public void close() {
        try { if (channel != null) channel.close(); } catch (IOException ignored) {}
        channel = null;
    }

    // ── сессия ───────────────────────────────────────────────────────────────

    public void setCredentials(String login, String password) {
        this.login    = login;
        this.password = password;
    }

    public void clearCredentials() {
        this.login    = null;
        this.password = null;
    }

    // ── высокоуровневые методы ────────────────────────────────────────────────

    public Response login(String login, String password) throws IOException, ClassNotFoundException {
        Request req = new Request(CommandType.LOGIN, login + " " + password, null, login, password);
        return send(req);
    }

    public Response register(String login, String password) throws IOException, ClassNotFoundException {
        Request req = new Request(CommandType.REGISTER, login + " " + password, null, login, password);
        return send(req);
    }

    public Response show() throws IOException, ClassNotFoundException {
        return send(authed(CommandType.SHOW, null, null));
    }

    public Response info() throws IOException, ClassNotFoundException {
        return send(authed(CommandType.INFO, null, null));
    }

    public Response checkInsertKey(int key) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.CHECK_INSERT_KEY, String.valueOf(key), null));
    }

    public Response checkUpdateKey(int key) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.CHECK_UPDATE_KEY, String.valueOf(key), null));
    }

    public Response insert(int key, Route route) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.INSERT, String.valueOf(key), route));
    }

    public Response update(int key, Route route) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.UPDATE, String.valueOf(key), route));
    }

    public Response removeKey(int key) throws IOException, ClassNotFoundException {
        return send(authed(CommandType.REMOVE_KEY, String.valueOf(key), null));
    }

    public Response clear() throws IOException, ClassNotFoundException {
        return send(authed(CommandType.CLEAR, null, null));
    }

    public Response averageOfDistance() throws IOException, ClassNotFoundException {
        return send(authed(CommandType.AVERAGE_OF_DISTANCE, null, null));
    }

    // ── внутренние ───────────────────────────────────────────────────────────

    private Request authed(CommandType type, String arg, Route route) {
        return new Request(type, arg, route, login, password);
    }

    public Response send(Request request) throws IOException, ClassNotFoundException {
        if (!isConnected()) connect();
        sendRequest(request);
        return receiveResponse();
    }

    private void sendRequest(Request request) throws IOException {
        byte[] data = serialize(request);
        int offset = 0;
        while (offset < data.length) {
            int len = Math.min(TcpUtil.CHUNK_SIZE, data.length - offset);
            writeAll(intToBytes(len));
            writeAll(data, offset, len);
            offset += len;
        }
        writeAll(intToBytes(0)); // маркер конца
    }

    private Response receiveResponse() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream acc = new ByteArrayOutputStream();
        while (true) {
            byte[] lenBytes = readExactly(4);
            int chunkLen = ByteBuffer.wrap(lenBytes).getInt();
            if (chunkLen == 0) break;
            if (chunkLen < 0) throw new IOException("Invalid chunk: " + chunkLen);
            acc.write(readExactly(chunkLen));
        }
        return (Response) deserialize(acc.toByteArray());
    }

    private void writeAll(byte[] bytes) throws IOException {
        writeAll(bytes, 0, bytes.length);
    }

    private void writeAll(byte[] bytes, int offset, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(bytes, offset, length);
        while (buf.hasRemaining()) channel.write(buf);
    }

    private byte[] readExactly(int n) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(n);
        while (buf.hasRemaining()) {
            int r = channel.read(buf);
            if (r == -1) throw new EOFException("Server closed connection");
            if (r == 0) {
                try { Thread.sleep(1); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while reading");
                }
            }
        }
        return buf.array();
    }

    private static byte[] intToBytes(int v) {
        return ByteBuffer.allocate(4).putInt(v).array();
    }

    private static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(b)) { oos.writeObject(o); }
        return b.toByteArray();
    }

    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ois.readObject();
        }
    }
}package org.Gh0st1yAnge1.gui;

import org.Gh0st1yAnge1.gui.localization.LocaleManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Точка входа JavaFX приложения.
 * Запуск: mvn javafx:run -pl client-gui
 * или:    java --module-path ... -jar client-gui.jar
 */
public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        LocaleManager.getInstance().setLocale(LocaleManager.RU);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle(LocaleManager.getInstance().get("app.title"));
        primaryStage.setScene(new Scene(root, 420, 400));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}package org.Gh0st1yAnge1.gui.visualization;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Назначает стабильный цвет каждому владельцу (owner_id).
 * Цвета берутся из заранее подобранной палитры, циклически.
 */
public class ColorManager {

    private static final Color[] PALETTE = {
            Color.web("#4FC3F7"),   // sky blue
            Color.web("#81C784"),   // green
            Color.web("#FFB74D"),   // amber
            Color.web("#F06292"),   // pink
            Color.web("#CE93D8"),   // purple
            Color.web("#4DB6AC"),   // teal
            Color.web("#FF8A65"),   // deep orange
            Color.web("#90A4AE"),   // blue grey
    };

    private final Map<Long, Color> cache = new HashMap<>();
    private int nextIndex = 0;

    /** Возвращает цвет для данного owner_id. Одному owner всегда один цвет. */
    public Color colorFor(long ownerId) {
        return cache.computeIfAbsent(ownerId, id -> {
            Color c = PALETTE[nextIndex % PALETTE.length];
            nextIndex++;
            return c;
        });
    }

    public void reset() {
        cache.clear();
        nextIndex = 0;
    }
}package org.Gh0st1yAnge1.gui.visualization;

import org.Gh0st1yAnge1.gui.models.RouteTableItem;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Рисует объекты коллекции на Canvas.
 * - Позиция зависит от координат X, Y
 * - Размер зависит от distance
 * - Цвет зависит от owner_id
 * - Пульсация реализована через AnimationTimer
 */
public class CanvasRenderer {

    private static final double MIN_RADIUS = 8;
    private static final double MAX_RADIUS = 40;
    private static final double PULSE_AMP  = 3.0;  // амплитуда пульсации в px
    private static final double PULSE_FREQ = 2.0;  // Гц

    private final Canvas        canvas;
    private final ColorManager colorManager = new ColorManager();
    private final AnimationTimer timer;

    private List<RouteTableItem> items = new ArrayList<>();
    private Consumer<RouteTableItem> onClickHandler;
    private long myOwnerId = -1;

    // Кэш позиций для hit-test при клике
    private record CircleInfo(RouteTableItem item, double cx, double cy, double baseR) {}
    private List<CircleInfo> circles = new ArrayList<>();

    public CanvasRenderer(Canvas canvas) {
        this.canvas = canvas;

        // AnimationTimer вызывается каждый кадр (~60fps)
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                drawFrame(now);
            }
        };
        timer.start();

        canvas.setOnMouseClicked(e -> handleClick(e.getX(), e.getY()));
    }

    public void setItems(List<RouteTableItem> items) {
        this.items = new ArrayList<>(items);
        colorManager.reset();
    }

    public void setMyOwnerId(long id) { this.myOwnerId = id; }

    public void setOnClick(Consumer<RouteTableItem> handler) {
        this.onClickHandler = handler;
    }

    public void stop() { timer.stop(); }

    // ── рисование ────────────────────────────────────────────────────────────

    private void drawFrame(long nowNanos) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Фон
        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(0, 0, w, h);

        // Сетка
        gc.setStroke(Color.web("#ffffff10"));
        gc.setLineWidth(0.5);
        for (double x = 0; x < w; x += 40) gc.strokeLine(x, 0, x, h);
        for (double y = 0; y < h; y += 40) gc.strokeLine(0, y, w, y);

        if (items.isEmpty()) {
            gc.setFill(Color.web("#ffffff44"));
            gc.setFont(Font.font(14));
            gc.fillText("No routes to display", w / 2 - 70, h / 2);
            return;
        }

        // Вычисляем диапазон координат для масштабирования
        double minX = items.stream().mapToDouble(RouteTableItem::getCoordX).min().orElse(0);
        double maxX = items.stream().mapToDouble(RouteTableItem::getCoordX).max().orElse(1);
        double minY = items.stream().mapToDouble(RouteTableItem::getCoordY).min().orElse(0);
        double maxY = items.stream().mapToDouble(RouteTableItem::getCoordY).max().orElse(1);
        double minD = items.stream().mapToDouble(RouteTableItem::getDistance).min().orElse(0);
        double maxD = items.stream().mapToDouble(RouteTableItem::getDistance).max().orElse(1);

        double rangeX = (maxX - minX) == 0 ? 1 : maxX - minX;
        double rangeY = (maxY - minY) == 0 ? 1 : maxY - minY;
        double rangeD = (maxD - minD) == 0 ? 1 : maxD - minD;

        double padding = 50;
        double t = nowNanos / 1_000_000_000.0; // секунды

        List<CircleInfo> newCircles = new ArrayList<>();

        for (RouteTableItem item : items) {
            // Позиция (с отступами)
            double cx = padding + (item.getCoordX() - minX) / rangeX * (w - 2 * padding);
            double cy = padding + (item.getCoordY() - minY) / rangeY * (h - 2 * padding);

            // Базовый радиус по distance
            double norm = (item.getDistance() - minD) / rangeD;
            double baseR = MIN_RADIUS + norm * (MAX_RADIUS - MIN_RADIUS);

            // Пульсация — каждый объект сдвинут по фазе на key
            double phase = item.getKey() * 0.7;
            double pulse = Math.sin(2 * Math.PI * PULSE_FREQ * t + phase) * PULSE_AMP;
            double r = baseR + pulse;

            Color baseColor = colorManager.colorFor(item.getOwnerId());
            boolean isMine  = item.getOwnerId() == myOwnerId;

            // Тень/свечение
            gc.setFill(baseColor.deriveColor(0, 1, 0.6, 0.3));
            gc.fillOval(cx - r * 1.5, cy - r * 1.5, r * 3, r * 3);

            // Основной круг
            gc.setFill(baseColor.deriveColor(0, 1, 1, 0.85));
            gc.fillOval(cx - r, cy - r, r * 2, r * 2);

            // Обводка — своё ярче
            gc.setStroke(isMine ? Color.WHITE : baseColor.brighter());
            gc.setLineWidth(isMine ? 2.0 : 1.0);
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

            // Подпись ключа
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(10));
            gc.fillText(String.valueOf(item.getKey()), cx - 4, cy + 4);

            newCircles.add(new CircleInfo(item, cx, cy, baseR));
        }

        circles = newCircles;
    }

    private void handleClick(double mx, double my) {
        if (onClickHandler == null) return;
        // Ищем ближайший объект
        for (CircleInfo ci : circles) {
            double dist = Math.hypot(mx - ci.cx(), my - ci.cy());
            if (dist <= ci.baseR() + PULSE_AMP + 2) {
                onClickHandler.accept(ci.item());
                return;
            }
        }
    }
}package org.Gh0st1yAnge1.gui.localization;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Singleton-менеджер локализации.
 * Поддерживаемые локали: ru, cs, it, es_DO.
 */
public class LocaleManager {

    public static final Locale RU    = new Locale("ru");
    public static final Locale CS    = new Locale("cs");
    public static final Locale IT    = new Locale("it");
    public static final Locale ES_DO = new Locale("es", "DO");

    public static final List<Locale> SUPPORTED = List.of(RU, CS, IT, ES_DO);

    private static final String BUNDLE_BASE = "org/Gh0st1yAnge1/gui/localization/messages";

    private static final LocaleManager INSTANCE = new LocaleManager();

    private final ObjectProperty<Locale> currentLocale =
            new SimpleObjectProperty<>(RU);

    private ResourceBundle bundle;

    private LocaleManager() {
        setLocale(RU);
    }

    public static LocaleManager getInstance() { return INSTANCE; }

    public void setLocale(Locale locale) {
        currentLocale.set(locale);
        bundle = ResourceBundle.getBundle(BUNDLE_BASE, locale);
    }

    public String get(String key) {
        try { return bundle.getString(key); }
        catch (Exception e) { return "?" + key + "?"; }
    }

    public Locale getCurrent()                        { return currentLocale.get(); }
    public ObjectProperty<Locale> localeProperty()    { return currentLocale; }

    public NumberFormat numberFormat() {
        return NumberFormat.getNumberInstance(currentLocale.get());
    }

    public DateTimeFormatter dateFormatter() {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(currentLocale.get());
    }

    /** Человекочитаемое название локали для ComboBox */
    public static String displayName(Locale locale) {
        if (locale.equals(RU))    return "Русский";
        if (locale.equals(CS))    return "Čeština";
        if (locale.equals(IT))    return "Italiano";
        if (locale.equals(ES_DO)) return "Español (DO)";
        return locale.toString();
    }
}package org.Gh0st1yAnge1.gui.controllers;

import org.Gh0st1yAnge1.gui.localization.LocaleManager;
import org.Gh0st1yAnge1.gui.network.ServerConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.Gh0st1yAnge1.request_and_response.Response;

public class AuthController {

    @FXML private Label          lblTitle;
    @FXML private Label          lblLogin;
    @FXML private Label          lblPassword;
    @FXML private TextField      tfLogin;
    @FXML private PasswordField  pfPassword;
    @FXML private Button         btnLogin;
    @FXML private Button         btnRegister;
    @FXML private Label          lblMessage;
    @FXML private ComboBox<String> cbLanguage;
    @FXML private Label          lblLanguage;

    private final LocaleManager lm         = LocaleManager.getInstance();
    private final ServerConnection connection = new ServerConnection();

    @FXML
    public void initialize() {
        // Языковой ComboBox
        cbLanguage.setItems(FXCollections.observableArrayList(
                LocaleManager.SUPPORTED.stream()
                        .map(LocaleManager::displayName)
                        .toList()
        ));
        cbLanguage.getSelectionModel().select(
                LocaleManager.SUPPORTED.indexOf(lm.getCurrent())
        );
        cbLanguage.setOnAction(e -> {
            int idx = cbLanguage.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                lm.setLocale(LocaleManager.SUPPORTED.get(idx));
                applyLocale();
            }
        });

        btnLogin.setOnAction(e -> onLogin());
        btnRegister.setOnAction(e -> onRegister());
        applyLocale();
    }

    private void applyLocale() {
        lblTitle.setText(lm.get("auth.title"));
        lblLogin.setText(lm.get("auth.login"));
        lblPassword.setText(lm.get("auth.password"));
        btnLogin.setText(lm.get("auth.btn.login"));
        btnRegister.setText(lm.get("auth.btn.register"));
        lblLanguage.setText(lm.get("auth.language"));
    }

    private void onLogin() {
        String login = tfLogin.getText().trim();
        String pass  = pfPassword.getText();
        if (login.isEmpty() || pass.isEmpty()) {
            showMessage(lm.get("auth.error.empty"), true);
            return;
        }
        setDisabled(true);
        new Thread(() -> {
            try {
                if (!connection.isConnected()) connection.connect();
                Response resp = connection.login(login, pass);
                Platform.runLater(() -> {
                    setDisabled(false);
                    if (resp.success()) {
                        connection.setCredentials(login, pass);
                        openMainWindow(login);
                    } else {
                        showMessage(resp.message() != null
                                ? resp.message() : lm.get("auth.error.failed"), true);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setDisabled(false);
                    showMessage(lm.get("error.connection") + ": " + ex.getMessage(), true);
                });
            }
        }, "auth-thread").start();
    }

    private void onRegister() {
        String login = tfLogin.getText().trim();
        String pass  = pfPassword.getText();
        if (login.isEmpty() || pass.isEmpty()) {
            showMessage(lm.get("auth.error.empty"), true);
            return;
        }
        setDisabled(true);
        new Thread(() -> {
            try {
                if (!connection.isConnected()) connection.connect();
                Response resp = connection.register(login, pass);
                Platform.runLater(() -> {
                    setDisabled(false);
                    if (resp.success()) {
                        showMessage(lm.get("auth.success.register"), false);
                    } else {
                        showMessage(resp.message() != null
                                ? resp.message() : lm.get("auth.error.failed"), true);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setDisabled(false);
                    showMessage(lm.get("error.connection") + ": " + ex.getMessage(), true);
                });
            }
        }, "register-thread").start();
    }

    private void openMainWindow(String login) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            MainController mc = loader.getController();
            mc.init(connection, login);

            Stage stage = new Stage();
            stage.setTitle(lm.get("app.title"));
            stage.setScene(new Scene(root, 1200, 750));
            stage.setOnCloseRequest(e -> mc.shutdown());
            stage.show();

            // Закрываем окно авторизации
            ((Stage) btnLogin.getScene().getWindow()).close();
        } catch (Exception ex) {
            showMessage("Failed to open main window: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    private void showMessage(String text, boolean error) {
        lblMessage.setText(text);
        lblMessage.setStyle(error ? "-fx-text-fill: #ff6b6b;" : "-fx-text-fill: #69db7c;");
    }

    private void setDisabled(boolean v) {
        tfLogin.setDisable(v);
        pfPassword.setDisable(v);
        btnLogin.setDisable(v);
        btnRegister.setDisable(v);
    }
}package org.Gh0st1yAnge1.gui.controllers;

import org.Gh0st1yAnge1.gui.localization.LocaleManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.Gh0st1yAnge1.model.Coordinates;
import org.Gh0st1yAnge1.model.Location;
import org.Gh0st1yAnge1.model.Route;

import java.time.LocalDate;

public class EditDialogController {

    @FXML private Label     lblTitle;
    @FXML private TextField tfKey;
    @FXML private TextField tfName;
    @FXML private TextField tfCoordX;
    @FXML private TextField tfCoordY;
    @FXML private TextField tfFromX;
    @FXML private TextField tfFromY;
    @FXML private TextField tfFromZ;
    @FXML private TextField tfFromName;
    @FXML private TextField tfToX;
    @FXML private TextField tfToY;
    @FXML private TextField tfToZ;
    @FXML private TextField tfToName;
    @FXML private TextField tfDistance;
    @FXML private Button    btnSave;
    @FXML private Button    btnCancel;
    @FXML private Label     lblError;

    private final LocaleManager lm = LocaleManager.getInstance();

    private boolean editMode = false;  // false = add, true = edit
    private int     fixedKey = -1;     // в режиме edit ключ фиксирован

    private Route  resultRoute;
    private int    resultKey;
    private boolean saved = false;

    @FXML
    public void initialize() {
        applyLocale();
        btnSave.setOnAction(e -> onSave());
        btnCancel.setOnAction(e -> close());
    }

    /** Заполнить диалог для редактирования существующего маршрута. */
    public void setForEdit(int key, Route route) {
        editMode = true;
        fixedKey = key;
        lblTitle.setText(lm.get("dialog.edit.title"));
        tfKey.setText(String.valueOf(key));
        tfKey.setDisable(true);
        if (route != null) fill(route);
    }

    /** Пустой диалог для добавления. */
    public void setForAdd() {
        editMode = false;
        lblTitle.setText(lm.get("dialog.add.title"));
        tfKey.setDisable(false);
    }

    public boolean isSaved()    { return saved; }
    public Route  getRoute()    { return resultRoute; }
    public int    getKey()      { return resultKey; }

    // ── private ──────────────────────────────────────────────────────────────

    private void fill(Route r) {
        tfName.setText(r.getName() != null ? r.getName() : "");
        if (r.getCoordinates() != null) {
            tfCoordX.setText(String.valueOf(r.getCoordinates().getX()));
            tfCoordY.setText(String.valueOf(r.getCoordinates().getY()));
        }
        tfDistance.setText(String.valueOf(r.getDistance()));
        if (r.getFrom() != null) {
            tfFromX.setText(String.valueOf(r.getFrom().getX()));
            tfFromY.setText(String.valueOf(r.getFrom().getIntY()));
            if (r.getFrom().getIntZ() != null) tfFromZ.setText(String.valueOf(r.getFrom().getIntZ()));
            if (r.getFrom().getName() != null)  tfFromName.setText(r.getFrom().getName());
        }
        if (r.getTo() != null) {
            tfToX.setText(String.valueOf(r.getTo().getX()));
            tfToY.setText(String.valueOf(r.getTo().getIntY()));
            if (r.getTo().getIntZ() != null) tfToZ.setText(String.valueOf(r.getTo().getIntZ()));
            if (r.getTo().getName() != null)  tfToName.setText(r.getTo().getName());
        }
    }

    private void onSave() {
        lblError.setText("");
        try {
            // Ключ
            int key;
            if (editMode) {
                key = fixedKey;
            } else {
                if (tfKey.getText().isBlank()) { lblError.setText(lm.get("dialog.error.key")); return; }
                key = Integer.parseInt(tfKey.getText().trim());
            }

            // Название
            if (tfName.getText().isBlank()) { lblError.setText(lm.get("dialog.error.name")); return; }

            // Координаты
            if (tfCoordX.getText().isBlank() || tfCoordY.getText().isBlank()) {
                lblError.setText(lm.get("dialog.error.coords")); return;
            }
            float cX = Float.parseFloat(tfCoordX.getText().trim());
            float cY = Float.parseFloat(tfCoordY.getText().trim());

            // Дистанция
            if (tfDistance.getText().isBlank()) { lblError.setText(lm.get("dialog.error.distance")); return; }
            float distance = Float.parseFloat(tfDistance.getText().trim());
            if (distance <= 0) { lblError.setText(lm.get("dialog.error.distance")); return; }

            // From (опционально)
            Location from = null;
            if (!tfFromX.getText().isBlank() || !tfFromY.getText().isBlank()) {
                double fx = Double.parseDouble(tfFromX.getText().trim());
                double fy = Double.parseDouble(tfFromY.getText().trim());
                Integer fz = tfFromZ.getText().isBlank() ? null : Integer.parseInt(tfFromZ.getText().trim());
                String  fn = tfFromName.getText().isBlank() ? null : tfFromName.getText().trim();
                from = new Location(fx, fy, fz == null ? 0 : fz);
                if (fn != null) from.setName(fn);
            }

            // To (опционально)
            Location to = null;
            if (!tfToX.getText().isBlank() || !tfToY.getText().isBlank()) {
                double tx = Double.parseDouble(tfToX.getText().trim());
                double ty = Double.parseDouble(tfToY.getText().trim());
                Integer tz = tfToZ.getText().isBlank() ? null : Integer.parseInt(tfToZ.getText().trim());
                String  tn = tfToName.getText().isBlank() ? null : tfToName.getText().trim();
                to = new Location(tx, ty, tz == null ? 0 : tz);
                if (tn != null) to.setName(tn);
            }

            Route r = new Route();
            r.setName(tfName.getText().trim());
            r.setCoordinates(new Coordinates(cX, cY));
            r.setDistance(distance);
            r.setFrom(from);
            r.setTo(to);
            r.setCreationDate(LocalDate.now());

            resultRoute = r;
            resultKey   = key;
            saved       = true;
            close();

        } catch (NumberFormatException ex) {
            lblError.setText(lm.get("dialog.error.coords"));
        }
    }

    private void applyLocale() {
        btnSave.setText(lm.get("dialog.btn.save"));
        btnCancel.setText(lm.get("dialog.btn.cancel"));
    }

    private void close() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }
}package org.Gh0st1yAnge1.gui.controllers;

import org.Gh0st1yAnge1.gui.visualization.CanvasRenderer;
import org.Gh0st1yAnge1.gui.localization.LocaleManager;
import org.Gh0st1yAnge1.gui.models.RouteTableItem;
import org.Gh0st1yAnge1.gui.network.ServerConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MainController {

    // ── TableView ────────────────────────────────────────────────────────────
    @FXML private TableView<RouteTableItem>          tableView;
    @FXML private TableColumn<RouteTableItem, Integer> colKey;
    @FXML private TableColumn<RouteTableItem, String>  colName;
    @FXML private TableColumn<RouteTableItem, Float>   colCoordX;
    @FXML private TableColumn<RouteTableItem, Float>   colCoordY;
    @FXML private TableColumn<RouteTableItem, String>  colDate;
    @FXML private TableColumn<RouteTableItem, String>  colFrom;
    @FXML private TableColumn<RouteTableItem, String>  colTo;
    @FXML private TableColumn<RouteTableItem, Float>   colDistance;
    @FXML private TableColumn<RouteTableItem, Long>    colOwner;

    // ── Toolbar / filters ────────────────────────────────────────────────────
    @FXML private Label           lblUser;
    @FXML private Button          btnAdd;
    @FXML private Button          btnEdit;
    @FXML private Button          btnDelete;
    @FXML private Button          btnRefresh;
    @FXML private Button          btnClear;
    @FXML private Button          btnLogout;
    @FXML private Button          btnInfo;
    @FXML private TextField       tfFilter;
    @FXML private ComboBox<String> cbFilterColumn;
    @FXML private ComboBox<String> cbSortColumn;
    @FXML private ComboBox<String> cbSortDir;
    @FXML private Label           lblStatus;
    @FXML private Label           lblCanvasInfo;

    // ── Canvas ───────────────────────────────────────────────────────────────
    @FXML private Canvas canvas;

    // ── internal ─────────────────────────────────────────────────────────────
    private ServerConnection connection;
    private String           currentLogin;
    private long             myOwnerId = -1;

    private final LocaleManager lm = LocaleManager.getInstance();

    private final ObservableList<RouteTableItem> allItems      = FXCollections.observableArrayList();
    private final ObservableList<RouteTableItem> filteredItems = FXCollections.observableArrayList();

    private CanvasRenderer renderer;
    private ScheduledExecutorService  scheduler;

    // ── init ──────────────────────────────────────────────────────────────────

    public void init(ServerConnection connection, String login) {
        this.connection   = connection;
        this.currentLogin = login;
        lblUser.setText(MessageFormat.format(lm.get("main.user"), login));

        setupTable();
        setupFilters();
        setupCanvas();
        setupButtons();
        applyLocale();

        // Периодический опрос каждые 2 секунды
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "refresh-scheduler");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::backgroundRefresh, 0, 2, TimeUnit.SECONDS);
    }

    public void shutdown() {
        if (scheduler != null) scheduler.shutdownNow();
        if (renderer  != null) renderer.stop();
        if (connection != null) connection.close();
    }

    // ── table setup ──────────────────────────────────────────────────────────

    private void setupTable() {
        colKey.setCellValueFactory(new PropertyValueFactory<>("key"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCoordX.setCellValueFactory(new PropertyValueFactory<>("coordX"));
        colCoordY.setCellValueFactory(new PropertyValueFactory<>("coordY"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        colFrom.setCellValueFactory(new PropertyValueFactory<>("from"));
        colTo.setCellValueFactory(new PropertyValueFactory<>("to"));
        colDistance.setCellValueFactory(new PropertyValueFactory<>("distance"));
        colOwner.setCellValueFactory(new PropertyValueFactory<>("ownerId"));

        tableView.setItems(filteredItems);
        tableView.setRowFactory(tv -> {
            TableRow<RouteTableItem> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openEditDialog(row.getItem());
                }
            });
            return row;
        });
    }

    // ── filter / sort ────────────────────────────────────────────────────────

    private static final List<String> COLUMN_NAMES =
            List.of("key","name","coordX","coordY","creationDate","from","to","distance","ownerId");

    private void setupFilters() {
        // Колонки для фильтра
        List<String> colLabels = COLUMN_NAMES.stream()
                .map(c -> lm.get("col." + c.replace("creationDate","date")
                        .replace("coordX","coordX").replace("coordY","coordY")
                        .replace("ownerId","owner")))
                .toList();
        cbFilterColumn.setItems(FXCollections.observableArrayList(colLabels));
        cbFilterColumn.getSelectionModel().selectFirst();

        cbSortColumn.setItems(FXCollections.observableArrayList(colLabels));
        cbSortColumn.getSelectionModel().selectFirst();

        cbSortDir.setItems(FXCollections.observableArrayList(
                lm.get("main.sort.asc"), lm.get("main.sort.desc")));
        cbSortDir.getSelectionModel().selectFirst();

        tfFilter.textProperty().addListener((obs, o, n) -> applyFilterAndSort());
        cbFilterColumn.setOnAction(e -> applyFilterAndSort());
        cbSortColumn.setOnAction(e -> applyFilterAndSort());
        cbSortDir.setOnAction(e -> applyFilterAndSort());
    }

    private void applyFilterAndSort() {
        String filterText = tfFilter.getText().trim().toLowerCase();
        int    filterIdx  = cbFilterColumn.getSelectionModel().getSelectedIndex();
        int    sortIdx    = cbSortColumn.getSelectionModel().getSelectedIndex();
        boolean asc       = cbSortDir.getSelectionModel().getSelectedIndex() == 0;

        // Filter via Streams API (требование лабы)
        List<RouteTableItem> result = allItems.stream()
                .filter(item -> {
                    if (filterText.isEmpty()) return true;
                    String val = getFieldValue(item, filterIdx);
                    return val.toLowerCase().contains(filterText);
                })
                .sorted((a, b) -> {
                    int cmp = compareField(a, b, sortIdx);
                    return asc ? cmp : -cmp;
                })
                .collect(Collectors.toList());

        filteredItems.setAll(result);
        renderer.setItems(filteredItems);
    }

    private String getFieldValue(RouteTableItem item, int colIdx) {
        return switch (colIdx) {
            case 0  -> String.valueOf(item.getKey());
            case 1  -> item.getName();
            case 2  -> String.valueOf(item.getCoordX());
            case 3  -> String.valueOf(item.getCoordY());
            case 4  -> item.creationDateProperty().get();
            case 5  -> item.fromProperty().get();
            case 6  -> item.toProperty().get();
            case 7  -> String.valueOf(item.getDistance());
            case 8  -> String.valueOf(item.getOwnerId());
            default -> "";
        };
    }

    @SuppressWarnings("unchecked")
    private int compareField(RouteTableItem a, RouteTableItem b, int colIdx) {
        return switch (colIdx) {
            case 0  -> Integer.compare(a.getKey(),      b.getKey());
            case 1  -> a.getName().compareTo(b.getName());
            case 2  -> Float.compare(a.getCoordX(),     b.getCoordX());
            case 3  -> Float.compare(a.getCoordY(),     b.getCoordY());
            case 4  -> a.creationDateProperty().get()
                    .compareTo(b.creationDateProperty().get());
            case 5  -> a.fromProperty().get().compareTo(b.fromProperty().get());
            case 6  -> a.toProperty().get().compareTo(b.toProperty().get());
            case 7  -> Float.compare(a.getDistance(),   b.getDistance());
            case 8  -> Long.compare(a.getOwnerId(),     b.getOwnerId());
            default -> 0;
        };
    }

    // ── canvas ───────────────────────────────────────────────────────────────

    private void setupCanvas() {
        renderer = new CanvasRenderer(canvas);
        renderer.setOnClick(item -> {
            String msg = MessageFormat.format(lm.get("canvas.click"),
                    item.getName(), lm.numberFormat().format(item.getDistance()), item.getKey());
            Platform.runLater(() -> lblCanvasInfo.setText(msg));
        });
    }

    // ── buttons ──────────────────────────────────────────────────────────────

    private void setupButtons() {
        btnAdd.setOnAction(e -> onAdd());
        btnEdit.setOnAction(e -> {
            RouteTableItem sel = tableView.getSelectionModel().getSelectedItem();
            if (sel != null) openEditDialog(sel);
        });
        btnDelete.setOnAction(e -> {
            RouteTableItem sel = tableView.getSelectionModel().getSelectedItem();
            if (sel != null) onDelete(sel);
        });
        btnRefresh.setOnAction(e -> backgroundRefresh());
        btnClear.setOnAction(e -> onClear());
        btnLogout.setOnAction(e -> onLogout());
        btnInfo.setOnAction(e -> onInfo());
    }

    // ── add ──────────────────────────────────────────────────────────────────

    private void onAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_dialog.fxml"));
            Parent root = loader.load();
            EditDialogController dc = loader.getController();
            dc.setForAdd();

            Stage dlg = dialogStage(root, lm.get("dialog.add.title"));
            dlg.showAndWait();

            if (!dc.isSaved()) return;
            int   key   = dc.getKey();
            Route route = dc.getRoute();

            setStatus(lm.get("status.refreshing"));
            new Thread(() -> {
                try {
                    // Шаг 1: проверить ключ
                    Response check = connection.checkInsertKey(key);
                    if (!check.success()) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.WARNING,
                                check.message() != null ? check.message() : "Key exists"));
                        return;
                    }
                    // Шаг 2: вставить
                    Response resp = connection.insert(key, route);
                    Platform.runLater(() -> {
                        if (resp.success()) {
                            backgroundRefresh();
                        } else {
                            showAlert(Alert.AlertType.ERROR,
                                    resp.message() != null ? resp.message()
                                            : MessageFormat.format(lm.get("error.server"), "insert"));
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                            lm.get("error.connection") + ": " + ex.getMessage()));
                }
            }, "insert-thread").start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ── edit ─────────────────────────────────────────────────────────────────

    private void openEditDialog(RouteTableItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_dialog.fxml"));
            Parent root = loader.load();
            EditDialogController dc = loader.getController();
            dc.setForEdit(item.getKey(), item.getRoute());

            Stage dlg = dialogStage(root, lm.get("dialog.edit.title"));
            dlg.showAndWait();

            if (!dc.isSaved()) return;
            Route updated = dc.getRoute();
            int   key     = dc.getKey();

            setStatus(lm.get("status.refreshing"));
            new Thread(() -> {
                try {
                    Response resp = connection.update(key, updated);
                    Platform.runLater(() -> {
                        if (resp.success()) {
                            backgroundRefresh();
                        } else {
                            showAlert(Alert.AlertType.ERROR,
                                    resp.message() != null ? resp.message()
                                            : lm.get("error.notOwner"));
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                            lm.get("error.connection") + ": " + ex.getMessage()));
                }
            }, "update-thread").start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ── delete ───────────────────────────────────────────────────────────────

    private void onDelete(RouteTableItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(lm.get("delete.confirm.title"));
        confirm.setHeaderText(lm.get("delete.confirm.header"));
        confirm.setContentText(MessageFormat.format(lm.get("delete.confirm.content"), item.getKey()));
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        Response resp = connection.removeKey(item.getKey());
                        Platform.runLater(() -> {
                            if (resp.success()) backgroundRefresh();
                            else showAlert(Alert.AlertType.ERROR,
                                    resp.message() != null ? resp.message() : lm.get("error.notOwner"));
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                                lm.get("error.connection") + ": " + ex.getMessage()));
                    }
                }, "delete-thread").start();
            }
        });
    }

    // ── clear ────────────────────────────────────────────────────────────────

    private void onClear() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(lm.get("clear.confirm.title"));
        confirm.setHeaderText(lm.get("clear.confirm.header"));
        confirm.setContentText(lm.get("clear.confirm.content"));
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        Response resp = connection.clear();
                        Platform.runLater(() -> {
                            if (resp.success()) backgroundRefresh();
                            else showAlert(Alert.AlertType.ERROR,
                                    resp.message() != null ? resp.message() : "Error");
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                                lm.get("error.connection") + ": " + ex.getMessage()));
                    }
                }, "clear-thread").start();
            }
        });
    }

    // ── info ─────────────────────────────────────────────────────────────────

    private void onInfo() {
        new Thread(() -> {
            try {
                Response resp = connection.info();
                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle(lm.get("info.title"));
                    a.setHeaderText(null);
                    a.setContentText(resp.message() != null ? resp.message() : "No info");
                    a.showAndWait();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                        lm.get("error.connection") + ": " + ex.getMessage()));
            }
        }, "info-thread").start();
    }

    // ── logout ───────────────────────────────────────────────────────────────

    private void onLogout() {
        shutdown();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(lm.get("app.title"));
            stage.setScene(new Scene(root, 420, 400));
            stage.show();
            ((Stage) btnLogout.getScene().getWindow()).close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ── refresh ──────────────────────────────────────────────────────────────

    private void backgroundRefresh() {
        try {
            if (!connection.isConnected()) connection.connect();
            Response resp = connection.show();
            if (resp.success() && resp.collection() != null) {
                List<Route> routes = resp.collection();
                Platform.runLater(() -> {
                    allItems.clear();
                    for (Route route : routes) {
                        Long key = route.getKey();
                        if (key != null) {
                            allItems.add(new RouteTableItem(Math.toIntExact(key), route));
                        }
                    }
                    applyFilterAndSort();
                    renderer.setMyOwnerId(myOwnerId);
                    setStatus(lm.get("status.connected"));
                });
            }
        } catch (Exception ex) {
            Platform.runLater(() -> setStatus(lm.get("status.disconnected") + ": " + ex.getMessage()));
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void applyLocale() {
        btnAdd.setText(lm.get("main.btn.add"));
        btnEdit.setText(lm.get("main.btn.edit"));
        btnDelete.setText(lm.get("main.btn.delete"));
        btnRefresh.setText(lm.get("main.btn.refresh"));
        btnClear.setText(lm.get("main.btn.clear"));
        btnLogout.setText(lm.get("main.btn.logout"));

        colKey.setText(lm.get("col.key"));
        colName.setText(lm.get("col.name"));
        colCoordX.setText(lm.get("col.coordX"));
        colCoordY.setText(lm.get("col.coordY"));
        colDate.setText(lm.get("col.date"));
        colFrom.setText(lm.get("col.from"));
        colTo.setText(lm.get("col.to"));
        colDistance.setText(lm.get("col.distance"));
        colOwner.setText(lm.get("col.owner"));
    }

    private void setStatus(String s) {
        Platform.runLater(() -> lblStatus.setText(s));
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private Stage dialogStage(Parent root, String title) {
        Stage s = new Stage();
        s.initModality(Modality.APPLICATION_MODAL);
        s.setTitle(title);
        s.setScene(new Scene(root));
        return s;
    }
}package org.Gh0st1yAnge1;

import org.Gh0st1yAnge1.client_commands.write.Insert;
import org.Gh0st1yAnge1.client_commands.write.Update;
import org.Gh0st1yAnge1.exceptions.InputCancelledException;
import org.Gh0st1yAnge1.manager.ClientCommandManager;
import org.Gh0st1yAnge1.manager.InputManager;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;
import org.Gh0st1yAnge1.request_and_response.Response;
import org.Gh0st1yAnge1.utils.RouteBuilder;
import org.Gh0st1yAnge1.utils.TcpUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientApp {

    private static final String HOST               = "localhost";
    private static final int    PORT               = 12345;
    private static final long   RECONNECT_DELAY_MS = 3000;

    private static String  sessionLogin    = null;
    private static String  sessionPassword = null;
    private static boolean isAuthenticated = false;

    private static SocketChannel channel;

    private static InputManager        inputManager;
    private static RouteBuilder        routeBuilder;
    private static ClientCommandManager commandManager;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(ClientApp::closeQuietly));

        inputManager   = new InputManager();
        routeBuilder   = new RouteBuilder(inputManager);
        commandManager = new ClientCommandManager(inputManager, routeBuilder);

        ensureConnected();

        String line;
        System.out.print("> ");
        while ((line = inputManager.readline()) != null) {
            line = line.trim();
            if (line.isEmpty()) { System.out.print("> "); continue; }

            String name = line.split("\\s+", 2)[0];
            String arg  = line.contains(" ") ? line.substring(line.indexOf(' ') + 1).trim() : null;

            if (name.equals("exit")) break;

            if (name.equals("execute_script")) {
                commandManager.executeScript(arg, ClientApp::scriptSender);
                System.out.print("> ");
                continue;
            }

            Request request = commandManager.execute(line);
            if (request == null) { System.out.print("> "); continue; }

            CommandType type = request.commandType();
            boolean isAuth = type == CommandType.LOGIN || type == CommandType.REGISTER || type == CommandType.LOGOUT;

            if (!isAuthenticated && !isAuth && type != CommandType.HELP) {
                System.out.println("You must login or register first! Use: login <login> <password>");
                System.out.print("> ");
                continue;
            }

            if (type == CommandType.CHECK_INSERT_KEY || type == CommandType.CHECK_UPDATE_KEY) {
                handleInsertOrUpdate(type, arg);
                System.out.print("> ");
                continue;
            }

            Request toSend = isAuth ? request : withCreds(request);
            Response resp  = communicate(toSend);
            if (resp != null) {
                printResponse(resp);
                applyAuthSideEffects(request, resp);
            }
            System.out.print("> ");
        }

        closeQuietly();
    }

    private static void handleInsertOrUpdate(CommandType checkType, String arg) {
        boolean isInsert = (checkType == CommandType.CHECK_INSERT_KEY);

        Response checkResp = communicate(withCreds(new Request(checkType, arg, null)));
        if (checkResp == null) return;
        printResponse(checkResp);
        if (!checkResp.success()) return;

        Request built;
        try {
            built = isInsert
                    ? new Insert(routeBuilder).execute(arg)
                    : new Update(routeBuilder).execute(arg);
        } catch (InputCancelledException e) {
            System.out.println("Route building cancelled.");
            return;
        }

        Response resp = communicate(withCreds(built));
        if (resp != null) printResponse(resp);
    }

    private static Response scriptSender(Request request) throws IOException, ClassNotFoundException {
        boolean isAuth = request.commandType() == CommandType.LOGIN || request.commandType() == CommandType.REGISTER || request.commandType() == CommandType.LOGOUT;
        Request toSend = isAuth ? request : withCreds(request);
        Response resp  = communicate(toSend);
        if (resp != null) applyAuthSideEffects(request, resp);
        return resp;
    }

    private static Request withCreds(Request r) {
        return new Request(r.commandType(), r.argument(), r.route(), sessionLogin, sessionPassword);
    }

    private static void applyAuthSideEffects(Request req, Response resp) {
        if (resp == null) return;
        switch (req.commandType()) {
            case LOGIN -> {
                if (resp.success()) {
                    sessionLogin    = req.login();
                    sessionPassword = req.password();
                    isAuthenticated = true;
                }
            }
            case REGISTER -> {
                if (resp.success())
                    System.out.println("Now you can login with: login <login> <password>");
            }
            case LOGOUT -> {
                sessionLogin    = null;
                sessionPassword = null;
                isAuthenticated = false;
            }
            default -> {}
        }
    }

    private static void printResponse(Response resp) {
        if (resp.message() != null) System.out.println(resp.message());
        if (resp.collection() != null) {
            if (resp.collection().isEmpty()) {
                System.out.println("(collection is empty)");
            } else {
                for (Route route : resp.collection()) System.out.println(route);
            }
        }
    }

    private static Response communicate(Request request) {
        try {
            ensureConnected();
            send(request);
            return receive();
        } catch (IOException e) {
            System.out.println("Connection lost: " + e.getMessage());
            closeQuietly();
            return null;
        } catch (ClassNotFoundException e) {
            System.out.println("Protocol error: " + e.getMessage());
            return null;
        }
    }

    private static void ensureConnected() {
        while (!isConnected()) {
            try {
                channel = SocketChannel.open();
                channel.configureBlocking(false);
                channel.connect(new InetSocketAddress(HOST, PORT));

                while (!channel.finishConnect()) {
                    Thread.sleep(10);
                }

                System.out.println("Connected to server.");
            } catch (IOException e) {
                System.out.println("Server unavailable. Retrying in " + RECONNECT_DELAY_MS + " ms...");
                closeQuietly();
                try { Thread.sleep(RECONNECT_DELAY_MS); } catch (InterruptedException ignored) {}
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static boolean isConnected() {
        return channel != null && channel.isOpen() && channel.isConnected();
    }

    private static void send(Request request) throws IOException {
        byte[] data = serialize(request);

        int offset = 0;
        while (offset < data.length) {
            int chunkLen = Math.min(TcpUtil.CHUNK_SIZE, data.length - offset);

            writeAll(intToBytes(chunkLen));
            writeAll(data, offset, chunkLen);

            offset += chunkLen;
        }

        writeAll(intToBytes(0));
    }

    private static void writeAll(byte[] bytes) throws IOException {
        writeAll(bytes, 0, bytes.length);
    }

    private static void writeAll(byte[] bytes, int offset, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(bytes, offset, length);
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
    }

    private static Response receive() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream acc = new ByteArrayOutputStream();

        while (true) {
            byte[] lenBytes = readExactly(4);
            int chunkLen = ByteBuffer.wrap(lenBytes).getInt();

            if (chunkLen == 0) break;
            if (chunkLen < 0) throw new IOException("Invalid chunk size: " + chunkLen);

            byte[] chunk = readExactly(chunkLen);
            acc.write(chunk);
        }

        return (Response) deserialize(acc.toByteArray());
    }

    private static byte[] readExactly(int n) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(n);
        while (buf.hasRemaining()) {
            int read = channel.read(buf);
            if (read == -1) throw new EOFException("Server closed connection");
            if (read == 0) {
                try { Thread.sleep(1); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while reading", e);
                }
            }
        }
        return buf.array();
    }

    private static byte[] intToBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }

    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ois.readObject();
        }
    }

    private static void closeQuietly() {
        try { if (channel != null) channel.close(); } catch (IOException ignored) {}
        channel = null;
    }
}package org.Gh0st1yAnge1.manager;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.client_commands.*;
import org.Gh0st1yAnge1.client_commands.auth.Login;
import org.Gh0st1yAnge1.client_commands.auth.Logout;
import org.Gh0st1yAnge1.client_commands.auth.Register;
import org.Gh0st1yAnge1.client_commands.read.*;
import org.Gh0st1yAnge1.client_commands.write.*;
import org.Gh0st1yAnge1.exceptions.InputCancelledException;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;
import org.Gh0st1yAnge1.request_and_response.Response;
import org.Gh0st1yAnge1.utils.RouteBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClientCommandManager {


    @FunctionalInterface
    public interface RequestSender {
        Response send(Request request) throws IOException, ClassNotFoundException;
    }

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final InputManager inputManager;
    private final RouteBuilder routeBuilder;

    private final Set<String> scriptStack = new HashSet<>();

    public ClientCommandManager(InputManager inputManager, RouteBuilder routeBuilder) {
        this.inputManager = inputManager;
        this.routeBuilder = routeBuilder;

        // read
        commands.put("average_of_distance", new AverageOfDistance());
        commands.put("help", new Help());
        commands.put("info", new Info());
        commands.put("show", new Show());
        commands.put("count_by_distance", new CountByDistance());
        commands.put("filter_less_than_distance", new FilterLessThanDistance());
        commands.put("check_insert_key", new CheckInsertKey());
        commands.put("check_update_key", new CheckUpdateKey());

        //write
        commands.put("clear", new Clear());
        commands.put("remove_key", new RemoveKey());
        commands.put("remove_greater_key", new RemoveGreaterKey());
        commands.put("insert", new Insert(routeBuilder));
        commands.put("update", new Update(routeBuilder));
        commands.put("replace_if_lower", new ReplaceIfLower(routeBuilder));
        commands.put("remove_greater", new RemoveGreater(routeBuilder));

        //others
        commands.put("execute_script", new ExecuteScript());
        commands.put("exit", new Exit());

        //auth
        commands.put("login", new Login());
        commands.put("logout", new Logout());
        commands.put("register", new Register());

    }

    public Request execute(String input) {
        if (input == null || input.trim().isEmpty()) return null;

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0];
        String arg = parts.length > 1 ? parts[1] : null;

        Command command = commands.get(commandName);
        if (command == null) {
            System.out.println("Unknown command. Type 'help' to see available commands.");
            return null;
        }

        if (commandName.equals("execute_script")) {
            return new Request(CommandType.EXECUTE_SCRIPT, arg, null);
        }

        if (commandName.equals("insert")) {
            return new Request(CommandType.CHECK_INSERT_KEY, arg, null);
        }

        if (commandName.equals("update")) {
            return new Request(CommandType.CHECK_UPDATE_KEY, arg, null);
        }

        try {
            return command.execute(arg);
        } catch (InputCancelledException ex) {
            System.out.println("Command cancelled.");
        } catch (Exception e) {
            System.out.println("Error while executing command: " + e.getMessage());
        }
        return null;
    }

    public void executeScript(String fileName, RequestSender sender) {
        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("Script file name is required.");
            return;
        }

        try {
            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("Script file not found: " + fileName);
                return;
            }

            String canonicalPath = file.getCanonicalPath();

            if (scriptStack.contains(canonicalPath)) {
                System.out.println("Recursive script detected, skipping: " + fileName);
                scriptStack.clear();
                return;
            }

            scriptStack.add(canonicalPath);
            Scanner fileScanner = new Scanner(file);
            inputManager.pushScanner(fileScanner);

            System.out.println("Executing script: " + fileName);
            try {
                String line;
                while ((line = inputManager.readline()) != null) {
                    if (line.trim().isEmpty()) continue;
                    System.out.println("> " + line);

                    String[] parts = line.trim().split("\\s+", 2);
                    String commandName = parts[0];
                    String arg = parts.length > 1 ? parts[1] : null;

                    if (commandName.equals("execute_script")) {
                        executeScript(arg, sender);
                        continue;
                    }

                    if (commandName.equals("insert")||commandName.equals("update")) {
                        Request checkRequest;
                        if (commandName.equals("insert")){
                            checkRequest = new Request(CommandType.CHECK_INSERT_KEY, arg, null);
                        } else {
                            checkRequest = new Request(CommandType.CHECK_UPDATE_KEY, arg, null);
                        }

                        Response checkResponse = sender.send(checkRequest);
                        if (checkResponse == null) {
                            System.out.println("Server disconnected during script.");
                            return;
                        }
                        if (checkResponse.success()) {
                            System.out.println(checkResponse.message());
                            try {
                                Request insertRequest = new Insert(routeBuilder).execute(arg);
                                Response insertResponse = sender.send(insertRequest);
                                if (insertResponse == null) {
                                    System.out.println("Server disconnected during script.");
                                    return;
                                }
                                if (insertResponse.message() != null)
                                    System.out.println(insertResponse.message());
                            } catch (InputCancelledException e) {
                                System.out.println("Route building cancelled.");
                            }
                        } else {
                            System.out.println(checkResponse.message());
                        }
                        continue;
                    }

                    Request request = execute(line);
                    if (request == null) continue;

                    Response response = sender.send(request);
                    if (response == null) {
                        System.out.println("Server disconnected during script.");
                        return;
                    }
                    if (response.message() != null) System.out.println(response.message());
                }
            } finally {
                inputManager.popScanner();
                fileScanner.close();
                scriptStack.remove(canonicalPath);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error while executing script: " + e.getMessage());
        }
    }
}
package org.Gh0st1yAnge1.manager;

import org.Gh0st1yAnge1.exceptions.InputCancelledException;
import org.Gh0st1yAnge1.model.Coordinates;
import org.Gh0st1yAnge1.model.Location;
import org.Gh0st1yAnge1.utils.Validator;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

public class InputManager {

    private final Deque<Scanner> scannerStack = new ArrayDeque<>();

    public InputManager(){
        scannerStack.push(new Scanner(System.in));
    }

    private Scanner currentScanner(){
        return scannerStack.peek();
    }

    public String readline(){

        if (isScriptMode()){
            Scanner scanner = currentScanner();
            if (!scanner.hasNextLine()){
                return null;
            }
            return scanner.nextLine().trim();
        }

        StringBuilder buffer = new StringBuilder();

        try{
            while (true){
                int ch = System.in.read();

                if (ch == -1){
                    return null;
                }

                if (ch == 27) {           //ESC
                    System.in.read();     //[
                    System.in.read();     //A/B/C/D
                    continue;
                }

                //Enter
                if (ch == '\n' || ch == '\r'){
                    System.out.println();
                    return buffer.toString().trim();
                }

                //Ctrl+C
                if (ch == 3){
                    System.out.println("Watafa");
                    return null;
                }

                //Ctrl+D
                if (ch == 4){
                    System.out.println("Yooooo, buddy, what's going on?\nU wanted to use 'Ctrl+D'?\nOh nooo, it doesn't works(\nChill out baby)");
                    return null;
                }

                //Ctrl+Z
                if (ch == 26){
                    System.out.println("Pepe shneine");
                    return null;
                }

                //Backspace
                if (ch == 127 || ch == 8){
                    if (buffer.length() > 0){
                        buffer.deleteCharAt(buffer.length()-1);
                        System.out.print("\b \b");
                    }
                    continue;
                }

                buffer.append((char)ch);
            }
        } catch (IOException e){
            return null;
        }
    }

    public void pushScanner(Scanner scanner){
        scannerStack.push(scanner);
    }

    public void popScanner(){
        if (scannerStack.size() > 1){
            scannerStack.pop();
        }
    }

    public boolean isScriptMode(){
        return scannerStack.size() > 1;
    }

    public <T> T readValue(
            String prompt,
            Function<String, T> parser,
            Predicate<T> validator,
            String errormessage
    ){
        while (true){

            if (!isScriptMode()){
                System.out.print(prompt);
            }

            String input = readline();

            if (input == null){
                throw new InputCancelledException("");
            }

            if(errormessage.contains("and have less than 5 digits after the dot")){
                if (input.contains(",")){
                    input = input.replace(',','.');
                }

                if (!input.matches("\\d+(\\.\\d{1,5})?")){
                    System.out.println(errormessage);
                    continue;
                }
            }

            try{
                T value = parser.apply(input);
                if (validator == null || validator.test(value)){
                    return value;
                }
            } catch (Exception ignored){}

            if (isScriptMode()){
                throw new RuntimeException("Invalid value in script.");
            }

            System.out.println(errormessage);
        }
    }
    //Location +

    public String readLocationName(){
        if (!isScriptMode()){
            System.out.println("Press 'Enter' to insert null or type location name: ");
        }

        String input = readline();

        if (input == null){
            throw new InputCancelledException("");
        }

        if (input.isEmpty()){
            return null;
        }

        return input;
    }

    public Double readLocationDoubleX(){
        return readValue(
                "Enter the 'double' type coordinate X: ",
                Double::parseDouble,
                null,
                "Coordinate X must have type 'double' and have less than 5 digits after the dot."
        );
    }

    public Double readLocationDoubleY(){
        return readValue(
                "Enter the 'float' type coordinate Y: ",
                Double::parseDouble,
                null,
                "Coordinate Y must have type 'float' and have less than 5 digits after the dot!"
        );
    }

    public Integer readLocationIntegerZ(){
        return readValue(
                "Enter the 'Integer' type coordinate Z: ",
                Integer::parseInt,
                Validator::validateLocationZ,
                "Coordinate Z must have type 'Integer'."
        );
    }


    //Coordinates +

    public Float readCoordinateFloatX(){
        return readValue(
                "Enter the 'Float' type coordinate X: ",
                Float::parseFloat,
                Validator::validateCoordinateX,
                "Coordinate X must have type 'Float' and have less than 5 digits after the dot."
        );
    }

    public Float readCoordinateFloatY(){
        return readValue(
                "Enter the 'Float' type coordinate Y: ",
                Float::parseFloat,
                Validator::validateCoordinateY,
                "Coordinate Y must have type 'float' and have less than 5 digits after the dot."
        );
    }

    //Route +

    public String readRouteName(){
        return readValue(
                "Enter the Route name: ",
                Function.identity(),
                Validator::validateRouteName,
                "Route name must not be empty!"
        );
    }

    public Float readRouteDistance(){
        return readValue(
                "Enter the 'float' type Route distance: ",
                Float::parseFloat,
                Validator::validateRouteDistance,
                "Route distance must have type 'float'."
        );
    }

    public Coordinates readCoordinates(){
        return new Coordinates(readCoordinateFloatX(), readCoordinateFloatY());
    }

    public Location readLocation(){

        Location location = null;
        if (!isScriptMode()){
            System.out.println("Press 'Enter' to insert null or type 'location' to create Location");
        }

        while(true){
            String line = readline();

            if (line == null){
                throw new InputCancelledException("");
            }
            if (line.trim().isEmpty()){
                return location;
            } else if (line.trim().equals("location")) {
                break;
            } else {
                System.out.println("Press 'Enter' or type 'location'");
            }
        }

        System.out.println("Creating Location...");

        int type = readValue(
                "Choose type '1' or '2':\n1 - (x, y, z)\n2 - (x, y, z, name)\n",
                Integer::parseInt,
                Validator::validateLocationType,
                "Type '1' or '2'"
        );

        switch (type){
            case 1 -> location = new Location(readLocationDoubleX(), readLocationDoubleY(), readLocationIntegerZ());
            case 2 -> location = new Location(readLocationDoubleX(), readLocationDoubleY(), readLocationIntegerZ(), readLocationName());
        }
        return location;
    }
}