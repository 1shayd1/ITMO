package org.shayd1.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager implements AutoCloseable {

    private final HikariDataSource dataSource;

    public DatabaseManager(String url, String user, String password) throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(5000);

        this.dataSource = new HikariDataSource(config);
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
        // Теперь соединение выдается из пула моментально
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
            System.out.println("DatabaseManager closed (HikariCP pool shutdown)");
        }
    }
}