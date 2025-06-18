package rs.jamie.luneth.modules;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SQLModule implements Module {

    private static final Map<String, String> DRIVER_MAP = Map.ofEntries(
            Map.entry("h2", "org.h2.Driver"),
            Map.entry("mariadb", "org.mariadb.jdbc.Driver"),
            Map.entry("mysql", "com.mysql.cj.jdbc.Driver"),
            Map.entry("sqlite", "org.sqlite.JDBC"),
            Map.entry("postgresql", "org.postgresql.Driver"),
            Map.entry("oracle", "oracle.jdbc.OracleDriver"),
            Map.entry("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver")
    );

    private final Connection conn ;

    public SQLModule(String url) {
        Connection c;
        try {
            registerDrivers(url);
            c = DriverManager.getConnection(url);
        } catch (Exception e) {
            c = null;
            e.printStackTrace();
        }
        conn = c;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.conn.close();
            } catch (Exception ignored) {}
        }));
    }

    private void registerDrivers(String jdbcUrl) throws ClassNotFoundException {
        String driver = getDriverClassName(jdbcUrl);
        if (driver != null) {
            Class.forName(driver);
        } else {
            throw new IllegalArgumentException("No driver found for JDBC URL: " + jdbcUrl);
        }
    }

    private static String getDriverClassName(String jdbcUrl) {
        for (Map.Entry<String, String> entry : DRIVER_MAP.entrySet()) {
            if (jdbcUrl.contains(":" + entry.getKey() + ":")) {
                return entry.getValue();
            }
        }
        return null;
    }


    @Override
    public CompletableFuture<ByteBuffer> getObject(ByteBuffer key, String identifier) {
        if (key==null) throw new IllegalArgumentException("Invalid Key");
        if (conn==null) throw new IllegalArgumentException("Database Connection is null");
        if (!identifier.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + identifier);
        }
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT \"value\" FROM " + identifier + " WHERE \"key\" = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBytes(1, key.array());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        byte[] valBytes = rs.getBytes("value");
                        return valBytes != null ? ByteBuffer.wrap(valBytes) : null;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> setObject(ByteBuffer key, ByteBuffer value, String identifier) {
        if (key==null) throw new IllegalArgumentException("Invalid Key");
        if (value==null) throw new IllegalArgumentException("Invalid Value");
        if (conn==null) throw new IllegalArgumentException("Database Connection is null");
        if (!identifier.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + identifier);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                String mergeSql = "MERGE INTO " + identifier + " (\"key\", \"value\") KEY(\"key\") VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(mergeSql)) {
                    ps.setBytes(1, key.array());
                    ps.setBytes(2, value.array());
                    ps.executeUpdate();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public void createTable(String name) {
        if (!name.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + name);
        }
        String sql = "CREATE TABLE IF NOT EXISTS " + name + " (" +
                "\"key\" VARBINARY PRIMARY KEY, " +
                "\"value\" BLOB" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
