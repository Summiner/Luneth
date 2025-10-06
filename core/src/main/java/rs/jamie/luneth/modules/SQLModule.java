package rs.jamie.luneth.modules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SQLModule implements Module {

    private final Connection conn;

    private enum DBMS {
        SQLITE("sqlite", "org.sqlite.JDBC"),
        MYSQL("mysql", "com.mysql.cj.jdbc.Driver"),
        POSTGRESQL("postgresql", "org.postgresql.Driver"),
        H2("h2", "org.h2.Driver"),
        HSQLDB("hsqldb", "org.hsqldb.jdbc.JDBCDriver"),
        DERBY("derby", "org.apache.derby.jdbc.EmbeddedDriver"),
        MARIADB("mariadb", "org.mariadb.jdbc.Driver"),
        ORACLE("oracle", "oracle.jdbc.OracleDriver"),
        SQLSERVER("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
        DB2("db2", "com.ibm.db2.jcc.DB2Driver"),
        SYBASE("sybase", "com.sybase.jdbc4.jdbc.SybDriver"),
        INFORMIX("informix", "com.informix.jdbc.IfxDriver"),
        FIREBIRD("firebird", "org.firebirdsql.jdbc.FBDriver"),
        INTERBASE("interbase", "interbase.interclient.Driver");

        private final String value;
        private final String className;

        DBMS(String value, String className) {
            this.value = value;
            this.className = className;
        }

        public String getValue() {
            return value;
        }

        public String getClassName() {
            return className;
        }

        public static void loadFromJdbc(@NotNull String url) {
            url = url.toLowerCase();
            for(DBMS driver : DBMS.values()) {
                if (url.contains(":" + driver.getValue() + ":")) {
                    try {
                        Class.forName(driver.className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Driver (" + driver.value + ") not found in classpath", e);
                    }
                    return;
                }
            }
            throw new IllegalArgumentException("No driver found for JDBC URL: " + url);
        }
    }

    public SQLModule(String url) {
        try {
            DBMS.loadFromJdbc(url);
            conn = DriverManager.getConnection(url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to JDBC: " + url, e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                conn.close();
            } catch (Exception ignored) {}
        }));
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
                throw new RuntimeException("Error executing SQL:getObject()", e);
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
            String mergeSql = "MERGE INTO " + identifier + " (\"key\", \"value\") KEY(\"key\") VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(mergeSql)) {
                ps.setBytes(1, key.array());
                ps.setBytes(2, value.array());
                ps.executeUpdate();
                return true;
            } catch (Exception e) {
                throw new RuntimeException("Error executing SQL:setObject()", e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> removeObject(ByteBuffer key, String identifier) {
        if (key==null) throw new IllegalArgumentException("Invalid Key");
        if (conn==null) throw new IllegalArgumentException("Database Connection is null");
        if (!identifier.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + identifier);
        }

        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM " + identifier + " WHERE \"key\" = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBytes(1, key.array());
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                throw new RuntimeException("Error executing SQL:removeObject()", e);
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
            throw new RuntimeException("Error executing SQL:createTable()", e);
        }
    }
}
