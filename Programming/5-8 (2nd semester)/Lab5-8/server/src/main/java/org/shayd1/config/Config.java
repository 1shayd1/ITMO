package org.shayd1.config;

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
