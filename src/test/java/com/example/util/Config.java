package com.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public final class Config {

    private static final String BASE_URL_KEY = "baseUrl";
    private static final String ENV_BASE_URL_KEY = "BASE_URL";
    private static final Properties PROPS = new Properties();
    private static boolean loaded = false;

    private Config() {}

    public static synchronized void loadIfNeeded() {
        if (loaded) {
            return;
        }
        try (InputStream in = resourceStream("/config.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException ignored) {
            // Intentionally ignore: fall back to defaults / system props / env
        }
        loaded = true;
    }

    public static String getBaseUrl() {
        loadIfNeeded();
        // Priority: system property > env var > properties file
        String fromSysProp = System.getProperty(BASE_URL_KEY);
        if (isNonEmpty(fromSysProp)) return fromSysProp.trim();

        String fromEnv = System.getenv(ENV_BASE_URL_KEY);
        if (isNonEmpty(fromEnv)) return fromEnv.trim();

        String fromProps = PROPS.getProperty(BASE_URL_KEY);
        if (isNonEmpty(fromProps)) return fromProps.trim();

        return ""; // empty means "not configured"
    }

    private static InputStream resourceStream(String path) {
        return Optional.ofNullable(Config.class.getResourceAsStream(path)).orElse(null);
    }

    private static boolean isNonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}


