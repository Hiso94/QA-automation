package com.example.util;

import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

public final class Db {

    private static final Properties PROPS = new Properties();
    private static boolean loaded;

    private Db() {}

    public static synchronized void loadIfNeeded() {
        if (loaded) return;
        try (InputStream in = Db.class.getResourceAsStream("/db.properties")) {
            if (in != null) PROPS.load(in);
        } catch (IOException ignored) {}
        // allow overrides via system props / env
        overrideIfPresent("db.url", System.getProperty("db.url"), System.getenv("DB_URL"));
        overrideIfPresent("db.user", System.getProperty("db.user"), System.getenv("DB_USER"));
        overrideIfPresent("db.password", System.getProperty("db.password"), System.getenv("DB_PASSWORD"));
        overrideIfPresent("db.schema", System.getProperty("db.schema"), System.getenv("DB_SCHEMA"));
        loaded = true;
    }

    private static void overrideIfPresent(String key, String sysValue, String envValue) {
        if (sysValue != null && !sysValue.trim().isEmpty()) {
            PROPS.setProperty(key, sysValue.trim());
            return;
        }
        if (envValue != null && !envValue.trim().isEmpty()) {
            PROPS.setProperty(key, envValue.trim());
        }
    }

    public static boolean isConfigured() {
        loadIfNeeded();
        String url = PROPS.getProperty("db.url", "").trim();
        return !url.isEmpty();
    }

    public static Connection getConnection() throws SQLException {
        loadIfNeeded();
        String url = PROPS.getProperty("db.url");
        String user = PROPS.getProperty("db.user");
        String password = PROPS.getProperty("db.password");
        Assumptions.assumeTrue(url != null && !url.isBlank(), "DB not configured; skipping DB tests");
        return DriverManager.getConnection(url, user, password);
    }

    public static Optional<String> findCustomerIdByEmail(Connection conn, String schema, String email) throws SQLException {
        String sql = "select id from " + schema + ".customer where email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString(1));
                }
                return Optional.empty();
            }
        }
    }

    public static Optional<String> findCustomerNameById(Connection conn, String schema, String id) throws SQLException {
        String sql = "select name from " + schema + ".customer where id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString(1));
                }
                return Optional.empty();
            }
        }
    }
}


